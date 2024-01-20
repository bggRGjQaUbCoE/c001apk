package com.example.c001apk.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.database.BlackListDatabase
import com.example.c001apk.logic.database.BrowseHistoryDatabase
import com.example.c001apk.logic.database.FeedFavoriteDatabase
import com.example.c001apk.logic.database.SearchHistoryDatabase
import com.example.c001apk.logic.database.TopicBlackListDatabase
import com.example.c001apk.logic.model.AppItem
import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.HomeMenu
import com.example.c001apk.logic.model.MessageResponse
import com.example.c001apk.logic.model.TopicBean
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.logic.model.UpdateCheckResponse
import com.example.c001apk.logic.network.Repository
import com.example.c001apk.util.Utils
import com.example.c001apk.util.Utils.Companion.getBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import rikka.core.content.pm.longVersionCodeCompat


class AppViewModel : ViewModel() {

    var itemCount = 1
    var loadState = 1
    var isFollow = false
    var isTop = false
    var isEnable = false
    var listSize = -1
    var feedTypeName: String? = null
    var topReplyId: String? = null
    var commentStatusText: String? = null
    var errorMessage: String? = null
    val bHistoryList: ArrayList<Any> = ArrayList()
    var requestHash: String? = null
    var changeFirstItem = false
    var isRequestValidate = false
    var isGetCaptcha = false
    var isCreateFeed = false
    var isGetCheckLoginInfo = false
    var level: String? = null
    var bio: String? = null
    var loginTime: String? = null
    var like: String? = null
    var follow: String? = null
    var fans: String? = null
    var version: String? = null
    var logo: String? = null
    var size: String? = null
    var lastupdate: String? = null
    var dateLine = 0L
    var followType = false
    var isViewReply: Boolean? = null
    var isShowMoreReply = false
    var replyCount: String? = null
    var isRefreshReply = false
    var device: String? = null
    var avatar: String? = null
    var cover: String? = null
    var isNew = false
    var isPostLikeFeed = false
    var isPostUnLikeFeed = false
    var isPostLikeReply = false
    var isPostUnLikeReply = false
    var isPostReply = false
    var postFollowUnFollow = false

    var fuid: String? = null // feed user id

    //feed data
    var id: String? = null // feed id
    var uid: String? = null// feed user id
    var funame: String? = null
    var uname: String? = null// feed username //被回复用户name
    var rid: String? = null// 回复feed/reply id
    var ruid: String? = null// 被回复用户id
    var type: String? = null //feed reply

    var rPosition: Int? = null
    var firstVisibleItemPosition = 0
    var firstCompletelyVisibleItemPosition = 0
    var lastVisibleItemPosition = 0
    var likeReplyPosition = -1

    val feedContentList = ArrayList<FeedContentResponse>()
    val feedReplyList = ArrayList<TotalReplyResponse.Data>()
    val feedTopReplyList = ArrayList<TotalReplyResponse.Data>()

    var isRefreshing = true
    var isLoadMore = false
    var isEnd = false

    private val getFeedData = MutableLiveData<String>()
    var frid: String? = null
    val feedData = getFeedData.switchMap {
        Repository.getFeedContent(id.toString(), frid)
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
            id.toString(),
            listType,
            page,
            firstItem,
            lastItem,
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
    var likeReplyId: String? = null
    private val postLikeReplyData = MutableLiveData<String>()
    val likeReplyData = postLikeReplyData.switchMap {
        Repository.postLikeReply(likeReplyId.toString())
    }

    fun postLikeReply() {
        postLikeReplyData.value = postLikeReplyData.value
    }

    //unlike reply
    private val postUnLikeReplyData = MutableLiveData<String>()
    val unLikeReplyData = postUnLikeReplyData.switchMap {
        Repository.postUnLikeReply(likeReplyId.toString())
    }

    fun postUnLikeReply() {
        postUnLikeReplyData.value = postUnLikeReplyData.value
    }

    //like feed
    var likeFeedId: String? = null
    private val postLikeFeedData = MutableLiveData<String>()
    val likeFeedData = postLikeFeedData.switchMap {
        Repository.postLikeFeed(likeFeedId.toString())
    }

    fun postLikeFeed() {
        postLikeFeedData.value = postLikeFeedData.value
    }

    //unlike feed
    private val postUnLikeFeedData = MutableLiveData<String>()
    val unLikeFeedData = postUnLikeFeedData.switchMap {
        Repository.postUnLikeFeed(likeFeedId.toString())
    }

    fun postUnLikeFeed() {
        postUnLikeFeedData.value = postUnLikeFeedData.value
    }

    var replyData = HashMap<String, String>()

    private val postReplyLiveData = MutableLiveData<String>()

    val postReplyData = postReplyLiveData.switchMap {
        Repository.postReply(replyData, rid.toString(), type.toString())
    }

    fun postReply() {
        postReplyLiveData.value = postReplyLiveData.value
    }

    private val getAppInfoData = MutableLiveData<String>()

    val appInfoData = getAppInfoData.switchMap {
        Repository.getAppInfo(id.toString())
    }

    fun getAppInfo() {
        getAppInfoData.value = getAppInfoData.value
    }

    var packageName: String? = null
    var versionCode: String? = null

    private val getDownloadLinkData = MutableLiveData<String>()

    val downloadLinkData = getDownloadLinkData.switchMap {
        Repository.getAppDownloadLink(
            packageName.toString(),
            appId.toString(),
            versionCode.toString()
        )
    }

    fun getDownloadLink() {
        getDownloadLinkData.value = getDownloadLinkData.value
    }


    var likePosition = -1
    private val baseURL = "/page?url=/feed/apkCommentList?id="
    var appId: String? = null
    var isInit = true
    val appCommentList = ArrayList<HomeFeedResponse.Data>()
    var appCommentTitle = "最近回复"
    var appCommentSort: String? = null

    private val getAppCommentData = MutableLiveData<String>()

    val appCommentData = getAppCommentData.switchMap {
        Repository.getDataList(
            baseURL + appId + appCommentSort,
            appCommentTitle,
            subtitle,
            lastItem,
            page
        )
    }

    fun getAppComment() {
        getAppCommentData.value = getAppCommentData.value
    }

    val menuList = ArrayList<HomeMenu>()
    var tabList = ArrayList<String>()

    var isResume = true

    var barTitle: String? = null
    var url: String? = null
    var title: String? = null
    var productTitle = "最近回复"

    val carouselList = ArrayList<HomeFeedResponse.Data>()

    private val getCarouselData = MutableLiveData<String>()

    val carouselData = getCarouselData.switchMap {
        Repository.getDataList(url.toString(), title.toString(), subtitle, lastItem, page)
    }

    fun getCarouselList() {
        getCarouselData.value = getCarouselData.value
    }

    val dataList = ArrayList<HomeFeedResponse.Data>()

    private val getDataListData = MutableLiveData<String>()

    val listData = getDataListData.switchMap {
        url = when (type) {
            "feed" -> "/v6/user/feedList?showAnonymous=0&isIncludeTop=1"
            "follow" -> "/v6/user/followList"
            "fans" -> "/v6/user/fansList"
            "apk" -> {
                uid = ""
                "/v6/user/apkFollowList"
            }

            "forum" -> {
                uid = ""
                "/v6/user/forumFollowList"
            }

            "like" -> "/v6/user/likeList"

            "reply" -> "/v6/user/replyList"

            "replyToMe" -> "/v6/user/replyToMeList"

            "recentHistory" -> "/v6/user/recentHistoryList"

            else -> throw IllegalArgumentException("invalid type: $type")
        }
        Repository.getFollowList(url.toString(), uid.toString(), page)
    }

    fun getFeedList() {
        getDataListData.value = getDataListData.value
    }

    val messageList = ArrayList<MessageResponse.Data>()

    private val getMessageListData = MutableLiveData<String>()

    val messageData = getMessageListData.switchMap {
        Repository.getMessage(url.toString(), page)
    }

    fun getMessage() {
        getMessageListData.value = getMessageListData.value
    }

    private val preGetLoginParamLiveData = MutableLiveData<String>()

    val preGetLoginParamData = preGetLoginParamLiveData.switchMap {
        Repository.preGetLoginParam()
    }

    fun preGetLoginParam() {
        preGetLoginParamLiveData.value = preGetLoginParamLiveData.value
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

    private val getValidateCaptchaLiveData = MutableLiveData<String>()

    private val validateCaptchaBaseUrl = "/v6/account/captchaImage?"
    val validateCaptchaData = getValidateCaptchaLiveData.switchMap {
        Repository.getValidateCaptcha("$validateCaptchaBaseUrl${timeStamp}&w=270=&h=113")
    }

    fun getValidateCaptcha() {
        getValidateCaptchaLiveData.value = getValidateCaptchaLiveData.value
    }

    private val getProfileDataLiveData = MutableLiveData<String>()

    val profileDataLiveData = getProfileDataLiveData.switchMap {
        Repository.getProfile(uid.toString())
    }

    fun getProfile() {
        getProfileDataLiveData.value = getProfileDataLiveData.value
    }

    var key: String? = null
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
        Repository.getUserSpace(id.toString())
    }

    fun getUser() {
        getUserData.value = getUserData.value
    }


    val feedList = ArrayList<HomeFeedResponse.Data>()

    private val getUserFeedData = MutableLiveData<String>()

    val userFeedData = getUserFeedData.switchMap {
        Repository.getUserFeed(uid.toString(), page)
    }

    fun getUserFeed() {
        getUserFeedData.value = getUserFeedData.value
    }

    val dyhDataList = ArrayList<HomeFeedResponse.Data>()

    var dyhId: String? = null

    private val getDyhDetailLiveData = MutableLiveData<String>()

    val dyhDetailLiveData = getDyhDetailLiveData.switchMap {
        Repository.getDyhDetail(dyhId.toString(), type.toString(), page)
    }

    fun getDyhDetail() {
        getDyhDetailLiveData.value = getDyhDetailLiveData.value
    }

    var position: Int = 0
    var r2rPosition = 0

    val replyTotalList = ArrayList<TotalReplyResponse.Data>()

    private val getReplyTotalLiveData = MutableLiveData<String>()

    val replyTotalLiveData = getReplyTotalLiveData.switchMap {
        Repository.getReply2Reply(id.toString(), page)
    }

    fun getReplyTotal() {
        getReplyTotalLiveData.value = getReplyTotalLiveData.value
    }

    val appList = ArrayList<AppItem>()
    val items: MutableLiveData<ArrayList<AppItem>> = MutableLiveData()
    val updateCheckEncoded: MutableLiveData<String> = MutableLiveData()

    fun getItems(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val appList = context.packageManager
                .getInstalledApplications(PackageManager.GET_SHARED_LIBRARY_FILES)
            val newItems = ArrayList<AppItem>()
            val updateCheckJsonObject = JSONObject()

            for (info in appList) {
                if (((info.flags and ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM)) {
                    val packageInfo = context.packageManager.getPackageInfo(info.packageName, 0)

                    val appItem = AppItem().apply {
//                        icon = info.loadIcon(context.packageManager)
//                        appName = info.loadLabel(context.packageManager).toString()
                        packageName = info.packageName
                        versionName =
                            "${packageInfo.versionName}(${packageInfo.longVersionCodeCompat})"
                        lastUpdateTime = packageInfo.lastUpdateTime
                    }

                    if (appItem.packageName != "com.example.c001apk")
                        newItems.add(appItem)

                    if (info.packageName != "com.example.c001apk")
                        updateCheckJsonObject.put(
                            info.packageName,
                            "0,${packageInfo.longVersionCodeCompat},${Utils.getInstalledAppMd5(info)}"
                        )
                }
            }

            withContext(Dispatchers.Main) {
                items.value =
                    newItems.sortedByDescending { it.lastUpdateTime }.toCollection(ArrayList())
                updateCheckEncoded.value = updateCheckJsonObject.toString().getBase64(false)
            }
        }
    }

    val appsUpdate = ArrayList<UpdateCheckResponse.Data>()
    private val getAppsUpdateData = MutableLiveData<String>()

    val appsUpdateData = getAppsUpdateData.switchMap {
        updateCheckEncoded.value?.let { it1 -> Repository.getAppsUpdate(it1) }
    }

    fun getAppsUpdate() {
        getAppsUpdateData.value = getAppsUpdateData.value
    }

    val homeFeedList = ArrayList<HomeFeedResponse.Data>()
    var firstLaunch = 1
    var installTime: String? = null
    var firstItem: String? = null
    var lastItem: String? = null

    private val getHomeFeedData = MutableLiveData<String>()

    val homeFeedData = getHomeFeedData.switchMap {
        if (isRefreshing)
            Repository.getHomeFeed(page, firstLaunch, installTime.toString(), firstItem, null)
        else //if (isLoadMore)
            Repository.getHomeFeed(page, firstLaunch, installTime.toString(), null, lastItem)
    }

    fun getHomeFeed() {
        getHomeFeedData.value = getHomeFeedData.value
    }

    private val getDataListLiveData = MutableLiveData<String>()

    val dataListData = getDataListLiveData.switchMap {
        Repository.getDataList(url.toString(), title.toString(), subtitle, lastItem, page)
    }

    fun getDataList() {
        getDataListLiveData.value = getDataListLiveData.value
    }

    var isInitial = true

    val topicList: MutableList<TopicBean> = ArrayList()

    val topicDataList = ArrayList<HomeFeedResponse.Data>()

    private val getTopicDataLiveData = MutableLiveData<String>()

    val topicDataLiveData = getTopicDataLiveData.switchMap {
        Repository.getDataList(url.toString(), title.toString(), subtitle, lastItem, page)
    }

    fun getTopicData() {
        getTopicDataLiveData.value = getTopicDataLiveData.value
    }


    val countList = ArrayList<String>()
    val messCountList = ArrayList<Int>()

    var historyList = ArrayList<String>()

    val searchList = ArrayList<HomeFeedResponse.Data>()

    var feedType: String = "all"
    var sort: String = "default" //hot // reply
    var keyWord: String? = null
    var pageType: String? = null  //"tag"
    var pageParam: String? = null //topic title
    private var showAnonymous = -1

    private val getSearchData = MutableLiveData<String>()

    val searchData = getSearchData.switchMap {
        Repository.getSearch(
            type.toString(),
            feedType,
            sort,
            keyWord.toString(),
            pageType.toString(),
            pageParam.toString(),
            page,
            showAnonymous
        )
    }

    fun getSearch() {
        getSearchData.value = getSearchData.value
    }

    var subtitle: String? = null

    private val getTopicLayoutLiveData = MutableLiveData<String>()

    val topicLayoutLiveData = getTopicLayoutLiveData.switchMap {
        Repository.getTopicLayout(url.toString())
    }

    fun getTopicLayout() {
        getTopicLayoutLiveData.value = getTopicLayoutLiveData.value
    }

    private val getProductLayoutLiveData = MutableLiveData<String>()

    val productLayoutLiveData = getProductLayoutLiveData.switchMap {
        Repository.getProductLayout(id.toString())
    }

    fun getProductLayout() {
        getProductLayoutLiveData.value = getProductLayoutLiveData.value
    }

    private val postFollowUnFollowLiveData = MutableLiveData<String>()

    val postFollowUnFollowData = postFollowUnFollowLiveData.switchMap {
        Repository.postFollowUnFollow(url.toString(), uid.toString())
    }

    var createFeedData = HashMap<String, String?>()

    fun postFollowUnFollow() {
        postFollowUnFollowLiveData.value = postFollowUnFollowLiveData.value
    }

    private val postCreateFeedLiveData = MutableLiveData<String>()

    val postCreateFeedData = postCreateFeedLiveData.switchMap {
        Repository.postCreateFeed(createFeedData)
    }

    fun postCreateFeed() {
        postCreateFeedLiveData.value = postCreateFeedLiveData.value
    }

    var requestValidateData = HashMap<String, String?>()
    private val postRequestValidateLiveData = MutableLiveData<String>()

    val postRequestValidateData = postCreateFeedLiveData.switchMap {
        Repository.postRequestValidate(requestValidateData)
    }

    fun postRequestValidate() {
        postRequestValidateLiveData.value = postRequestValidateLiveData.value
    }


    val blackListLiveData: MutableLiveData<ArrayList<String>> = MutableLiveData()
    fun getBlackList(type: String, context: Context) {
        val searchHistoryDao = SearchHistoryDatabase.getDatabase(context).searchHistoryDao()
        val blackListDao = BlackListDatabase.getDatabase(context).blackListDao()
        val topicBlacklist = TopicBlackListDatabase.getDatabase(context).blackListDao()
        val newList = ArrayList<String>()
        viewModelScope.launch(Dispatchers.IO) {
            if (type == "history")
                for (element in searchHistoryDao.loadAllHistory()) {
                    newList.add(element.keyWord)
                }
            else if (type == "blacklist")
                for (element in blackListDao.loadAllList()) {
                    newList.add(element.keyWord)
                }
            else if (type == "topicBlacklist")
                for (element in topicBlacklist.loadAllList()) {
                    newList.add(element.keyWord)
                }
            withContext(Dispatchers.Main) {
                blackListLiveData.value = newList
            }
        }
    }

    val browseLiveData: MutableLiveData<ArrayList<Any>> = MutableLiveData()
    fun getBrowseList(type: String, context: Context) {
        val browseHistoryDao = BrowseHistoryDatabase.getDatabase(context).browseHistoryDao()
        val feedFavoriteDao = FeedFavoriteDatabase.getDatabase(context).feedFavoriteDao()
        val newList = ArrayList<Any>()
        viewModelScope.launch(Dispatchers.IO) {
            if (type == "browse")
                for (element in browseHistoryDao.loadAllHistory()) {
                    newList.add(element)
                }
            else
                for (element in feedFavoriteDao.loadAllHistory()) {
                    newList.add(element)
                }
            withContext(Dispatchers.Main) {
                browseLiveData.value = newList
            }
        }
    }

    var totalOptionNum = -1
    var currentOption = 0
    var extraKey: String? = null
    var leftEnd = false
    var rightEnd = false

    val leftVoteCommentList = ArrayList<TotalReplyResponse.Data>()
    val rightVoteCommentList = ArrayList<TotalReplyResponse.Data>()
    var voteCommentList = ArrayList<TotalReplyResponse.Data>()

    private val getVoteCommentListData = MutableLiveData<String>()

    val voteCommentData = getVoteCommentListData.switchMap {
        Repository.getVoteComment(id.toString(), extraKey.toString(), page, firstItem, lastItem)
    }

    fun getVoteComment() {
        getVoteCommentListData.value = getVoteCommentListData.value
    }

    private val getProductListData = MutableLiveData<String>()

    val productCategoryData = getProductListData.switchMap {
        Repository.getProductList()
    }

    fun getProductList() {
        getProductListData.value = getProductListData.value
    }

    var collectionUrl: String? = null
    var cId: String? = null
    var cUid: String? = null
    private val getCollectionListData = MutableLiveData<String>()

    val collectionListData = getCollectionListData.switchMap {
        Repository.getCollectionList(collectionUrl.toString(), cUid, cId, 0, page)
    }

    fun getCollectionList() {
        getCollectionListData.value = getCollectionListData.value
    }

    private val postDeleteLiveData = MutableLiveData<String>()

    val postDeleteData = postDeleteLiveData.switchMap {
        Repository.postDelete(url.toString(), deleteId.toString())
    }

    var deleteId: String? = null

    fun postDelete() {
        postDeleteLiveData.value = postDeleteLiveData.value
    }

    var postFollow = HashMap<String, String>()

    private val postFollowLiveData = MutableLiveData<String>()

    val postFollowData = postFollowLiveData.switchMap {
        Repository.postFollow(postFollow)
    }

    fun postFollow() {
        postFollowLiveData.value = postFollowLiveData.value
    }

    var tag: String? = null
    var fid: String? = null
    var followUrl: String? = null
    private val getFollowLiveData = MutableLiveData<String>()

    val getFollowData = getFollowLiveData.switchMap {
        Repository.getFollow(followUrl.toString(), tag, fid)
    }

    fun getFollow() {
        getFollowLiveData.value = getFollowLiveData.value
    }

}