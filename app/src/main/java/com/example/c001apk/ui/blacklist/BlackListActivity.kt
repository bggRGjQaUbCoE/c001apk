package com.example.c001apk.ui.blacklist

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityBlackListBinding
import com.example.c001apk.logic.model.StringEntity
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.feed.reply.IOnItemClickListener
import com.example.c001apk.ui.search.HistoryAdapter
import com.example.c001apk.ui.topic.TopicActivity
import com.example.c001apk.ui.user.UserActivity
import com.example.c001apk.util.IntentUtil
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class BlackListActivity : BaseActivity<ActivityBlackListBinding>(), IOnItemClickListener {

    private val viewModel by viewModels<BlackListViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<BlackListViewModel.Factory> { factory ->
                factory.create(type = intent.getStringExtra("type") ?: "user")
            }
        }
    )
    private lateinit var mAdapter: HistoryAdapter
    private lateinit var mLayoutManager: FlexboxLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initBar()
        initEditText()
        initEdit()
        initClearHistory()
        initObserve()

    }

    private fun initObserve() {
        viewModel.blackListLiveData.observe(this) {
            binding.indicator.isIndeterminate = false
            binding.indicator.isVisible = false
            mAdapter.submitList(it)
            binding.clearAll.isVisible = it.isNotEmpty()
        }

        viewModel.toastText.observe(this) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initBar() {
        binding.toolBar.apply {
            title = if (viewModel.type == "user") getString(R.string.user_black_list)
            else getString(R.string.topic_black_list)
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                finish()
            }
            inflateMenu(R.menu.blacklist_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.backup -> {
                        if (saveFile(
                                Gson().toJson(
                                    viewModel.blackListLiveData.value?.map { item ->
                                        item.data
                                    } ?: emptyList<String>()
                                )
                            )
                        )
                            backupSAFLauncher.launch(
                                if (viewModel.type == "user") "user_blacklist.json"
                                else "topic_blacklist.json"
                            )
                        else
                            Toast.makeText(this@BlackListActivity, "导出失败", Toast.LENGTH_SHORT)
                                .show()
                    }

                    R.id.restore -> {
                        restoreSAFLauncher.launch("application/json")
                    }
                }
                true
            }
        }
    }

    private fun saveFile(content: String): Boolean {
        return try {
            val dir = File(this.cacheDir.toString())
            if (!dir.exists())
                dir.mkdir()
            val file = File("${this.cacheDir}/blacklist.json")
            if (!file.exists())
                file.createNewFile()
            else {
                file.delete()
                file.createNewFile()
            }
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(content.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private val backupSAFLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) backup@{ uri ->
            if (uri == null) return@backup
            try {
                File("${this.cacheDir}/blacklist.json").inputStream().use { input ->
                    this.contentResolver.openOutputStream(uri).use { output ->
                        if (output == null) Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT)
                            .show()
                        else input.copyTo(output)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    private val restoreSAFLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) restore@{ uri ->
            if (uri == null) return@restore
            runCatching {
                val string = this.contentResolver
                    .openInputStream(uri)?.reader().use { it?.readText() }
                    ?: throw IOException("Backup file was damaged")
                val dataList: List<String> = Gson().fromJson(
                    string,
                    Array<String>::class.java
                ).toList()
                val currentList: List<String> =
                    viewModel.blackListLiveData.value?.map { it.data } ?: emptyList()
                val newList: List<String> =
                    if (currentList.isEmpty())
                        dataList
                    else
                        dataList.filter {
                            it !in currentList
                        }
                if (newList.isNotEmpty())
                    viewModel.insertList(newList.map {
                        StringEntity(it)
                    })
            }.onFailure {
                MaterialAlertDialogBuilder(this)
                    .setTitle("导入失败")
                    .setMessage(it.message)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton("Crash Log") { _, _ ->
                        MaterialAlertDialogBuilder(this)
                            .setTitle("Crash Log")
                            .setMessage(it.stackTraceToString())
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                    .show()
            }
        }


    private fun initClearHistory() {
        binding.clearAll.setOnClickListener {
            MaterialAlertDialogBuilder(this).apply {
                setTitle("确定清除全部黑名单？")
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.deleteAll()
                    binding.clearAll.isVisible = false
                }
                show()
            }
        }
    }

    private fun initView() {
        binding.indicator.isIndeterminate = true
        binding.indicator.isVisible = true
        mLayoutManager = FlexboxLayoutManager(this)
        mLayoutManager.flexDirection = FlexDirection.ROW
        mLayoutManager.flexWrap = FlexWrap.WRAP
        mAdapter = HistoryAdapter()
        mAdapter.setOnItemClickListener(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
        }
    }

    private fun initEditText() {
        binding.title.text = when (viewModel.type) {
            "user" -> this.getString(R.string.user_black_list)
            "topic" -> this.getString(R.string.topic_black_list)
            else -> ""
        }
        binding.editText.highlightColor = ColorUtils.setAlphaComponent(
            MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorPrimaryDark,
                0
            ), 128
        )
        binding.editText.hint = when (viewModel.type) {
            "user" -> "uid"
            "topic" -> "话题"
            else -> ""
        }
        binding.editText.isFocusable = true
        binding.editText.isFocusableInTouchMode = true
        binding.editText.requestFocus()
        val imm =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.editText, 0)
        binding.editText.imeOptions = EditorInfo.IME_ACTION_SEARCH
        binding.editText.inputType = when (viewModel.type) {
            "user" -> EditorInfo.TYPE_CLASS_NUMBER
            "topic" -> EditorInfo.TYPE_CLASS_TEXT
            else -> EditorInfo.TYPE_CLASS_TEXT
        }
    }

    private fun initEdit() {
        binding.editText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, keyEvent ->
            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEARCH) && keyEvent != null) {
                checkData()
                return@OnEditorActionListener true
            }
            false
        })
    }


    private fun checkData() {
        if (binding.editText.text.toString() == "") {
            return
        } else {
            insertData(binding.editText.text.toString())
            binding.editText.text = null
        }
    }

    private fun insertData(data: String) {
        viewModel.insertList(data)
    }

    override fun onItemClick(data: String) {
        when (viewModel.type) {
            "user" -> {
                IntentUtil.startActivity<UserActivity>(this) {
                    putExtra("id", data)
                }
            }

            "topic" -> {
                IntentUtil.startActivity<TopicActivity>(this) {
                    putExtra("type", "topic")
                    putExtra("title", data)
                    putExtra("url", data)
                    putExtra("id", "")
                }
            }
        }

    }

    override fun onItemDeleteClick(data: String) {
        viewModel.deleteData(data)
    }

}