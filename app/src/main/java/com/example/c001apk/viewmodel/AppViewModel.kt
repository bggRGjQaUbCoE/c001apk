package com.example.c001apk.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.AppItem
import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.logic.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AppViewModel : ViewModel() {

    var isRefreshReply = false
    var device = ""
    var avatar = ""
    var totalScrollY = 0
    var haveTop = false
    var isNew = true
    var isPostLikeFeed = false
    var isPostUnLikeFeed = false
    var isPostLikeReply = false
    var isPostUnLikeReply = false
    var isPostReply = false

    //feed data
    var id = "" // feed id
    var uid = "" // feed user id
    var uname = "" // feed username //被回复用户name
    var rid = "" // 回复feed/reply id
    var ruid = "" // 被回复用户id
    var type = "" //feed reply

    var rPosition = -1
    var replyAndForward = "0"
    var cursorBefore = -1
    var firstCompletelyVisibleItemPosition = -1
    var lastVisibleItemPosition = -1
    var likeReplyPosition = -1

    var replyTextMap: MutableMap<String, String> = HashMap()
    val feedContentList = ArrayList<FeedContentResponse>()
    val feedReplyList = ArrayList<TotalReplyResponse.Data>()

    var isRefreshing = true
    var isLoadMore = false
    var isEnd = false

    private val getFeedData = MutableLiveData<String>()

    val feedData = getFeedData.switchMap {
        Repository.getFeedContent(id)
    }

    fun getFeed() {
        getFeedData.value = getFeedData.value
    }

    var page = 1
    private var discussMode = 1
    var listType = "lastupdate_desc"
    private var blockStatus = 0
    var fromFeedAuthor = 0

    private val getFeedReplyData = MutableLiveData<String>()

    val feedReplyData = getFeedReplyData.switchMap {
        Repository.getFeedContentReply(
            id,
            listType,
            page,
            discussMode,
            "feed",
            blockStatus,
            fromFeedAuthor
        )
    }

    fun getFeedReply() {
        getFeedReplyData.value = getFeedReplyData.value
    }


    //like reply
    var likeReplyId = ""
    private val postLikeReplyData = MutableLiveData<String>()
    val likeReplyData = postLikeReplyData.switchMap {
        Repository.postLikeReply(likeReplyId)
    }

    fun postLikeReply() {
        postLikeReplyData.value = postLikeReplyData.value
    }

    //unlike reply
    private val postUnLikeReplyData = MutableLiveData<String>()
    val unLikeReplyData = postUnLikeReplyData.switchMap {
        Repository.postUnLikeReply(likeReplyId)
    }

    fun postUnLikeReply() {
        postUnLikeReplyData.value = postUnLikeReplyData.value
    }

    //like feed
    var likeFeedId = ""
    private val postLikeFeedData = MutableLiveData<String>()
    val likeFeedData = postLikeFeedData.switchMap {
        Repository.postLikeFeed(likeFeedId)
    }

    fun postLikeFeed() {
        postLikeFeedData.value = postLikeFeedData.value
    }

    //unlike feed
    private val postUnLikeFeedData = MutableLiveData<String>()
    val unLikeFeedData = postUnLikeFeedData.switchMap {
        Repository.postUnLikeFeed(likeFeedId)
    }

    fun postUnLikeFeed() {
        postUnLikeFeedData.value = postUnLikeFeedData.value
    }

    var replyData = HashMap<String, String>()

    private val postReplyLiveData = MutableLiveData<String>()

    val postReplyData = postReplyLiveData.switchMap {
        Repository.postReply(replyData, rid, type)
    }

    fun postReply() {
        postReplyLiveData.value = postReplyLiveData.value
    }

    private val getAppInfoData = MutableLiveData<String>()

    val appInfoData = getAppInfoData.switchMap {
        Repository.getAppInfo(id)
    }

    fun getAppInfo() {
        getAppInfoData.value = getAppInfoData.value
    }

    var likePosition = -1
    private val baseURL =
        "#/feed/apkCommentList?isIncludeTop=1&withSortCard=1&id="
    var appId = ""
    var isInit = true
    val appCommentList = ArrayList<HomeFeedResponse.Data>()

    private val getAppCommentData = MutableLiveData<String>()

    val appCommentData = getAppCommentData.switchMap {
        Repository.getDataList(baseURL + appId, "讨论", "", "", page)
    }

    fun getAppComment() {
        getAppCommentData.value = getAppCommentData.value
    }

    val tabList = ArrayList<String>()
    var fragmentList = ArrayList<Fragment>()

    var isResume = true

    var barTitle = ""
    var url = ""
    var title = ""

    val carouselList = ArrayList<HomeFeedResponse.Data>()

    private val getCarouselData = MutableLiveData<String>()

    val carouselData = getCarouselData.switchMap {
        Repository.getDataList(url, title, "", "", page)
    }

    fun getCarouselList() {
        getCarouselData.value = getCarouselData.value
    }


    val dataList = ArrayList<HomeFeedResponse.Data>()

    private val getDataListData = MutableLiveData<String>()

    val listData = getDataListData.switchMap {
        when (type) {
            "feed" -> Repository.getFeedList(uid, page)
            "follow" -> Repository.getFollowList(uid, page)
            else -> Repository.getFansList(uid, page)
        }
    }

    fun getFeedList() {
        getDataListData.value = getDataListData.value
    }

    private val getLoginParamData = MutableLiveData<String>()

    val loginParamData = getLoginParamData.switchMap {
        Repository.getLoginParam()
    }

    fun getLoginParam() {
        getLoginParamData.value = getLoginParamData.value
    }

    var loginData = HashMap<String, String?>()

    private val getTryLoginData = MutableLiveData<String>()

    val tryLoginData = getTryLoginData.switchMap {
        Repository.tryLogin(loginData)
    }

    fun tryLogin() {
        getTryLoginData.value = getTryLoginData.value
    }

    private val getCaptchaData = MutableLiveData<String>()

    var timeStamp = 0L
    private val baseUrl = "/auth/showCaptchaImage?"
    val captchaData = getCaptchaData.switchMap {
        Repository.getCaptcha("$baseUrl$timeStamp")
    }

    fun getCaptcha() {
        getCaptchaData.value = getCaptchaData.value
    }

    private val getProfileDataLiveData = MutableLiveData<String>()

    val profileDataLiveData = getProfileDataLiveData.switchMap {
        Repository.getProfile(uid)
    }

    fun getProfile() {
        getProfileDataLiveData.value = getProfileDataLiveData.value
    }

    var key = ""
    private val getSmsLoginParamLiveData = MutableLiveData<String>()

    val smsLoginParamData = getSmsLoginParamLiveData.switchMap {
        Repository.getSmsLoginParam("mobile")
    }

    fun getSmsLoginParam() {
        getSmsLoginParamLiveData.value = getSmsLoginParamLiveData.value
    }

    var getSmsData = HashMap<String, String?>()

    private val getSmsDataLiveData = MutableLiveData<String>()

    val getSmsTokenData = getSmsDataLiveData.switchMap {
        Repository.getSmsToken("mobile", getSmsData)
    }

    fun getSmsToken() {
        getSmsDataLiveData.value = getSmsDataLiveData.value
    }

    var badge = 0

    private val getCheckLoginInfoData = MutableLiveData<String>()

    val checkLoginInfoData = getCheckLoginInfoData.switchMap {
        Repository.checkLoginInfo()
    }

    fun getCheckLoginInfo() {
        getCheckLoginInfoData.value = getCheckLoginInfoData.value
    }

    private val getUserData = MutableLiveData<String>()

    val userData = getUserData.switchMap {
        Repository.getUserSpace(id)
    }

    fun getUser() {
        getUserData.value = getUserData.value
    }


    val feedList = ArrayList<HomeFeedResponse.Data>()

    private val getUserFeedData = MutableLiveData<String>()

    val userFeedData = getUserFeedData.switchMap {
        Repository.getUserFeed(uid, page)
    }

    fun getUserFeed() {
        getUserFeedData.value = getUserFeedData.value
    }

    val dyhDataList = ArrayList<HomeFeedResponse.Data>()

    var dyhId = ""

    private val getDyhDetailLiveData = MutableLiveData<String>()

    val dyhDetailLiveData = getDyhDetailLiveData.switchMap {
        Repository.getDyhDetail(dyhId, type, page)
    }

    fun getDyhDetail() {
        getDyhDetailLiveData.value = getDyhDetailLiveData.value
    }

    var position: Int = 0
    var r2rPosition = 0
    var isPaste = false

    val replyTotalList = ArrayList<TotalReplyResponse.Data>()

    private val getReplyTotalLiveData = MutableLiveData<String>()

    val replyTotalLiveData = getReplyTotalLiveData.switchMap {
        Repository.getReply2Reply(id, page)
    }

    fun getReplyTotal() {
        getReplyTotalLiveData.value = getReplyTotalLiveData.value
    }

    val appList = ArrayList<AppItem>()
    val items: MutableLiveData<ArrayList<AppItem>> = MutableLiveData()

    fun getItems(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val appList = context.packageManager
                .getInstalledApplications(PackageManager.GET_SHARED_LIBRARY_FILES)
            val newItems = ArrayList<AppItem>()

            for (info in appList) {
                if (((info.flags and ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM)) {

                    val appItem = AppItem().apply {
                        icon = info.loadIcon(context.packageManager)
                        appName = info.loadLabel(context.packageManager).toString()
                        packageName = info.packageName
                        val packageInfo = context.packageManager.getPackageInfo(info.packageName, 0)
                        versionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                            "${packageInfo.versionName}(${packageInfo.longVersionCode})"
                        else "${packageInfo.versionName}(${packageInfo.versionCode})"


                    }

                    newItems.add(appItem)
                }
            }

            withContext(Dispatchers.Main) {
                items.value = newItems
            }
        }
    }

    val homeFeedList = ArrayList<HomeFeedResponse.Data>()
    var firstLaunch = 1
    var installTime = ""
    var firstItem = ""
    var lastItem = ""

    private val getHomeFeedData = MutableLiveData<String>()

    val homeFeedData = getHomeFeedData.switchMap {
        Repository.getHomeFeed(page, firstLaunch, installTime, "", "")
    }

    fun getHomeFeed() {
        getHomeFeedData.value = getHomeFeedData.value
    }

    val followFeedList = ArrayList<HomeFeedResponse.Data>()

    private val getFollowFeedData = MutableLiveData<String>()

    val followFeedData = getFollowFeedData.switchMap {
        Repository.getDataList("/page?url=V9_HOME_TAB_FOLLOW", "关注", "", lastItem, page)
    }

    fun getFollowFeed() {
        getFollowFeedData.value = getFollowFeedData.value
    }

    val homeRankingList = ArrayList<HomeFeedResponse.Data>()

    private val getHomeRankingData = MutableLiveData<String>()

    val homeRankingData = getHomeRankingData.switchMap {
        Repository.getDataList("/page?url=V9_HOME_TAB_RANKING", "热榜", "", lastItem, page)
    }

    fun getHomeRanking() {
        getHomeRankingData.value = getHomeRankingData.value
    }

    var isInitial = true


    var titleList = ArrayList<String>()

    val homeTopicTitleList = ArrayList<HomeFeedResponse.Entities>()

    private val getHomeTopicTitleLiveData = MutableLiveData<String>()

    val homeTopicTitleLiveData = getHomeTopicTitleLiveData.switchMap {
        Repository.getDataList("/page?url=V11_VERTICAL_TOPIC", "话题", "", "", 1)
    }

    fun getHomeTopicTitle() {
        getHomeTopicTitleLiveData.value = getHomeTopicTitleLiveData.value
    }

    val topicDataList = ArrayList<HomeFeedResponse.Data>()

    private val getTopicDataLiveData = MutableLiveData<String>()

    val topicDataLiveData = getTopicDataLiveData.switchMap {
        Repository.getDataList(url, title, "", "", page)
    }

    fun getTopicData() {
        getTopicDataLiveData.value = getTopicDataLiveData.value
    }


    val countList = ArrayList<String>()

    var historyList = ArrayList<String>()

    val searchList = ArrayList<HomeFeedResponse.Data>()

    var feedType: String = "all"
    var sort: String = "default" //hot // reply
    var keyWord: String = ""

    private val getSearchData = MutableLiveData<String>()

    val searchData = getSearchData.switchMap {
        Repository.getSearch(type, feedType, sort, keyWord, page)
    }

    fun getSearch() {
        getSearchData.value = getSearchData.value
    }

    var subtitle: String? = null

    var tag = ""

    private val getTopicLayoutLiveData = MutableLiveData<String>()

    val topicLayoutLiveData = getTopicLayoutLiveData.switchMap {
        Repository.getTopicLayout(url)
    }

    fun getTopicLayout() {
        getTopicLayoutLiveData.value = getTopicLayoutLiveData.value
    }

    private val getProductLayoutLiveData = MutableLiveData<String>()

    val productLayoutLiveData = getProductLayoutLiveData.switchMap {
        Repository.getProductLayout(id)
    }

    fun getProductLayout() {
        getProductLayoutLiveData.value = getProductLayoutLiveData.value
    }

    val searchTabList = arrayOf("动态", "应用", "数码", "用户", "话题")
    var searchFragmentList = ArrayList<Fragment>()

}