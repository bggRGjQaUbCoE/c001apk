package com.example.c001apk.ui.blacklist

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModelProvider
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityBlackListBinding
import com.example.c001apk.logic.database.BlackListDatabase
import com.example.c001apk.logic.database.TopicBlackListDatabase
import com.example.c001apk.logic.model.SearchHistory
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.feed.reply.IOnItemClickListener
import com.example.c001apk.ui.search.HistoryAdapter
import com.example.c001apk.ui.search.SearchViewModel
import com.example.c001apk.ui.topic.TopicActivity
import com.example.c001apk.ui.user.UserActivity
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.Utils.getColorFromAttr
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class BlackListActivity : BaseActivity<ActivityBlackListBinding>(), IOnItemClickListener {

    private val viewModel by lazy { ViewModelProvider(this)[SearchViewModel::class.java] }
    private lateinit var mAdapter: HistoryAdapter
    private lateinit var mLayoutManager: FlexboxLayoutManager
    private val blackListDao by lazy {
        BlackListDatabase.getDatabase(this@BlackListActivity).blackListDao()
    }
    private val topicBlackListDao by lazy {
        TopicBlackListDatabase.getDatabase(this@BlackListActivity).blackListDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.type = intent.getStringExtra("type")

        initView()

        binding.indicator.isIndeterminate = true
        binding.indicator.visibility = View.VISIBLE
        if (viewModel.listSize == -1) {
            when (viewModel.type) {
                "user" -> viewModel.getBlackList("userBlacklist", this@BlackListActivity)
                "topic" -> viewModel.getBlackList("topicBlacklist", this@BlackListActivity)
            }
        }

        initBar()
        initButton()
        initEditText()
        initEdit()
        initClearHistory()

        viewModel.blackListLiveData.observe(this) {
            binding.indicator.isIndeterminate = false
            binding.indicator.visibility = View.GONE
            viewModel.listSize = it.size
            mAdapter.submitList(it)
            if (it.isEmpty())
                binding.clearAll.visibility = View.GONE
            else
                binding.clearAll.visibility = View.VISIBLE
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
                        if (saveFile(Gson().toJson(viewModel.blackListLiveData.value)))
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
                val json: Array<String> = Gson().fromJson(
                    string,
                    Array<String>::class.java
                )
                val currentList = viewModel.blackListLiveData.value?.toMutableList() ?: ArrayList()
                val newList = ArrayList<String>()
                if (currentList.isEmpty())
                    newList.addAll(json)
                else
                    json.forEach {
                        if (currentList.indexOf(it) == -1)
                            newList.add(it)
                    }
                if (newList.isNotEmpty()) {
                    if (currentList.isEmpty())
                        viewModel.blackListLiveData.postValue(newList)
                    else {
                        currentList.addAll(0, newList)
                        viewModel.blackListLiveData.postValue(currentList)
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        when (viewModel.type) {
                            "user" ->
                                blackListDao.insertAll(newList.map {
                                    SearchHistory(it)
                                })

                            "topic" ->
                                topicBlackListDao.insertAll(newList.map {
                                    SearchHistory(it)
                                })
                        }
                    }
                }
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

    private fun initButton() {
        /*binding.search.setOnClickListener {
            checkUid()
        }*/
    }

    private fun initClearHistory() {
        binding.clearAll.setOnClickListener {
            MaterialAlertDialogBuilder(this).apply {
                setTitle("确定清除全部黑名单？")
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        when (viewModel.type) {
                            "user" -> blackListDao.deleteAll()
                            "topic" -> topicBlackListDao.deleteAll()
                        }
                    }
                    viewModel.blackListLiveData.postValue(emptyList())
                    binding.clearAll.visibility = View.GONE
                }
                show()
            }
        }
    }

    private fun initView() {
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
            this.getColorFromAttr(
                rikka.preference.simplemenu.R.attr.colorPrimary
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
                checkUid()
                return@OnEditorActionListener true
            }
            false
        })
    }


    private fun checkUid() {
        if (binding.editText.text.toString() == "") {
            return
        } else {
            updateUid(binding.editText.text.toString())
            binding.editText.text = null
        }
    }

    private fun updateUid(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            when (viewModel.type) {
                "user" -> {
                    if (!blackListDao.isExist(uid))
                        blackListDao.insert(SearchHistory(uid))

                }

                "topic" -> {
                    if (!topicBlackListDao.isExist(uid))
                        topicBlackListDao.insert(SearchHistory(uid))
                }
            }
        }
        val blackList = viewModel.blackListLiveData.value?.toMutableList() ?: ArrayList()
        if (blackList.indexOf(uid) == -1) {
            blackList.add(0, uid)
            viewModel.blackListLiveData.postValue(blackList)
            if (binding.clearAll.visibility != View.VISIBLE)
                binding.clearAll.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "已存在", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemClick(keyword: String) {
        when (viewModel.type) {
            "user" -> {
                IntentUtil.startActivity<UserActivity>(this) {
                    putExtra("id", keyword)
                }
            }

            "topic" -> {
                IntentUtil.startActivity<TopicActivity>(this) {
                    putExtra("type", "topic")
                    putExtra("title", keyword)
                    putExtra("url", keyword)
                    putExtra("id", "")
                }
            }
        }

    }

    override fun onItemDeleteClick(position: Int, keyword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            when (viewModel.type) {
                "user" -> blackListDao.delete(keyword)
                "topic" -> topicBlackListDao.delete(keyword)
            }
        }
        val blackList = viewModel.blackListLiveData.value?.toMutableList() ?: ArrayList()
        blackList.removeAt(position)
        viewModel.blackListLiveData.postValue(blackList)
    }

}