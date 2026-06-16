package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentMediaViewerBinding
import com.futurecode.recoverdeletedmessages.utils.Constants
import java.io.File
import java.io.FileOutputStream

class MediaViewerFragment : BaseFragment<FragmentMediaViewerBinding>(FragmentMediaViewerBinding::inflate) {

    private var player: ExoPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mediaPath = arguments?.getString(Constants.ARG_MEDIA_PATH) ?: return
        val mediaType = arguments?.getString(Constants.ARG_MEDIA_TYPE) ?: Constants.MEDIA_TYPE_IMAGE
        val isContentUri = mediaPath.startsWith("content://")

        val fileName = if (isContentUri) {
            Uri.parse(mediaPath).lastPathSegment
                ?.let { Uri.decode(it).substringAfterLast('/') }
                ?: "media"
        } else {
            File(mediaPath).name
        }

        binding.tvFileName.text = fileName
        binding.ivBack.setOnClickListener { findNavController().popBackStack() }

        when {
            mediaType == Constants.MEDIA_TYPE_VIDEO -> {
                binding.ivMedia.visibility = View.GONE
                binding.playerView.visibility = View.VISIBLE
                initExoPlayer(mediaPath)
            }
            else -> {
                binding.ivMedia.visibility = View.VISIBLE
                binding.playerView.visibility = View.GONE
                val imageSource: Any = if (isContentUri) Uri.parse(mediaPath) else File(mediaPath)
                Glide.with(this).load(imageSource).into(binding.ivMedia)
            }
        }

        binding.btnSave.setOnClickListener { saveFile(mediaPath, fileName, isContentUri) }
        binding.btnShare.setOnClickListener { shareFile(mediaPath, fileName, isContentUri) }
        binding.btnDelete.setOnClickListener { deleteFile(mediaPath, isContentUri) }
    }

    private fun initExoPlayer(path: String) {
        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            val mediaItem = MediaItem.fromUri(Uri.parse(path))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    private fun saveFile(mediaPath: String, fileName: String, isContentUri: Boolean) {
        val destDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "RecoverDeletedMessages"
        )
        if (!destDir.exists()) destDir.mkdirs()
        val dest = File(destDir, fileName)

        try {
            if (isContentUri) {
                val inputStream = requireContext().contentResolver.openInputStream(Uri.parse(mediaPath))
                    ?: run {
                        Toast.makeText(requireContext(), "Cannot read file", Toast.LENGTH_SHORT).show()
                        return
                    }
                FileOutputStream(dest).use { out -> inputStream.use { it.copyTo(out) } }
            } else {
                val src = File(mediaPath)
                if (!src.exists()) {
                    Toast.makeText(requireContext(), "File not found", Toast.LENGTH_SHORT).show()
                    return
                }
                src.copyTo(dest, overwrite = true)
            }

            val destUri = FileProvider.getUriForFile(
                requireContext(), "${requireContext().packageName}.provider", dest
            )
            android.media.MediaScannerConnection.scanFile(
                requireContext(), arrayOf(dest.absolutePath), null, null
            )
            Toast.makeText(requireContext(), "Saved to ${dest.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(mediaPath: String, fileName: String, isContentUri: Boolean) {
        try {
            val shareUri: Uri = if (isContentUri) {
                Uri.parse(mediaPath)
            } else {
                val file = File(mediaPath)
                if (!file.exists()) {
                    Toast.makeText(requireContext(), "File not found", Toast.LENGTH_SHORT).show()
                    return
                }
                FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share via"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteFile(mediaPath: String, isContentUri: Boolean) {
        try {
            if (isContentUri) {
                val deleted = requireContext().contentResolver.delete(Uri.parse(mediaPath), null, null)
                if (deleted > 0) findNavController().popBackStack()
                else Toast.makeText(requireContext(), "Cannot delete this file", Toast.LENGTH_SHORT).show()
            } else {
                val file = File(mediaPath)
                if (file.exists() && file.delete()) findNavController().popBackStack()
                else Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        player?.release()
        player = null
        super.onDestroyView()
    }
}