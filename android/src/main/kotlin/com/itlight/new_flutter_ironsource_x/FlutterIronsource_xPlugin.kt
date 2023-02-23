package com.itlight.new_flutter_ironsource_x

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import com.ironsource.adapters.supersonicads.SupersonicConfig
import com.ironsource.mediationsdk.*
import com.ironsource.mediationsdk.impressionData.ImpressionData
import com.ironsource.mediationsdk.impressionData.ImpressionDataListener
import com.ironsource.mediationsdk.integration.IntegrationHelper
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.model.Placement
import com.ironsource.mediationsdk.sdk.*
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import java.util.*

/** FlutterIronsource_xPlugin */
class FlutterIronsource_xPlugin: FlutterPlugin, MethodCallHandler, ActivityAware, InterstitialListener, RewardedVideoListener, OfferwallListener, ImpressionDataListener {
  private lateinit var mActivity : Activity
  private lateinit var mChannel : MethodChannel
  private lateinit var messenger: BinaryMessenger
  private lateinit var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding

  val TAG = "IronsourcePlugin"
  var APP_KEY = ""
  lateinit var mPlacement: Placement
  val FALLBACK_USER_ID = "userId"
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == IronSourceConsts.INIT && call.hasArgument("appKey")) {
      call.argument<String>("appKey")?.let { appKey ->
        initializeIronsourceSDK(
          appKey,
          call.argument<Boolean>("gdprConsent") ?: false,
          call.argument<Boolean>("ccpaConsent") ?: false,
          call.argument<Boolean>("debugMode") ?: false,
        )
        result.success(null)
      } ?: run {
        result.error("APP_KEY_NOT_PROVIDED", "App key not provided for IronSource SDK initialization", null)
      }
    } else if (call.method == IronSourceConsts.LOAD_INTERSTITIAL) {
      IronSource.loadInterstitial()
      result.success(null)
    } else if (call.method == IronSourceConsts.SHOW_INTERSTITIAL) {
      IronSource.showInterstitial()
      result.success(null)
    } else if (call.method == IronSourceConsts.IS_INTERSTITIAL_READY) {
      result.success(IronSource.isInterstitialReady())
    } else if (call.method == IronSourceConsts.IS_REWARDED_VIDEO_AVAILABLE) {
      result.success(IronSource.isRewardedVideoAvailable())
    } else if (call.method == IronSourceConsts.IS_OFFERWALL_AVAILABLE) {
      result.success(IronSource.isOfferwallAvailable())
    } else if (call.method == IronSourceConsts.SHOW_OFFERWALL) {
      IronSource.showOfferwall()
      result.success(null)
    } else if (call.method == IronSourceConsts.SHOW_REWARDED_VIDEO) {
      IronSource.showRewardedVideo()
      result.success(null)
    } else if (call.method == "validateIntegration") {
      IntegrationHelper.validateIntegration(mActivity)
      result.success(null)
    } else if (call.method == "setUserId") {
      IronSource.setUserId(call.argument<String>("userId"))
      result.success(null)
    } else if (call.method == "getAdvertiserId") {
      result.success(IronSource.getAdvertiserId(mActivity))
    } else if (call.method == "activityResumed") {
      IronSource.onResume(mActivity)
      result.success(null)
    } else if (call.method == "activityPaused") {
      IronSource.onPause(mActivity)
      result.success(null)
    } else if (call.method == "shouldTrackNetworkState" && call.hasArgument("state")) {
      call.argument<Boolean>("state")?.let { IronSource.shouldTrackNetworkState(mActivity, it) }
      result.success(null)
    } else {
      result.notImplemented()
    }
  }
  
  private fun initializeIronsourceSDK(
    appKey: String,
    gdprConsent: Boolean,
    ccpaConsent: Boolean,
    debugMode: Boolean
  ) {
    IronSource.setInterstitialListener(this)
    IronSource.setRewardedVideoListener(this)
    IronSource.setOfferwall





  fun initialize(appKey: String, gdprConsent: Boolean, ccpaConsent: Boolean) {
    IronSource.setInterstitialListener(this)
    IronSource.setRewardedVideoListener(this)
    IronSource.setOfferwallListener(this)
    IronSource.setConsent(gdprConsent)
    IronSource.addImpressionDataListener(this)

    val config = IronSourceConfig()
    config.clientSideCallbacks = true
    IronSource.init(mActivity, appKey, IronSource.AD_UNIT.OFFERWALL, IronSource.AD_UNIT.INTERSTITIAL, IronSource.AD_UNIT.REWARDED_VIDEO, config)

    if (ccpaConsent) {
        val ccpaParams = HashMap<String, String>()
        ccpaParams[IronSourceConstants.IRONSOURCE_CCPA_CONSENT_STRING] = "1"
        IronSource.updateConsentInfo(ccpaParams)
    } else {
        val ccpaParams = HashMap<String, String>()
        ccpaParams[IronSourceConstants.IRONSOURCE_CCPA_CONSENT_STRING] = "0"
        IronSource.updateConsentInfo(ccpaParams)
    }
}

  // Interstitial Listener
  override fun onInterstitialAdClicked(instanceId: String) {
    mActivity.runOnUiThread { //back on UI thread...
        mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_CLICKED, null)
    }
}

override fun onInterstitialAdReady(instanceId: String) {
    mActivity.runOnUiThread { //back on UI thread...
        mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_READY, null)
    }
}

override fun onInterstitialAdLoadFailed(instanceId: String, ironSourceError: IronSourceError) {
    mActivity.runOnUiThread { //back on UI thread...
        val arguments = hashMapOf<String, Any>()
        arguments["errorCode"] = ironSourceError.errorCode
        arguments["errorMessage"] = ironSourceError.errorMessage
        mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_LOAD_FAILED, arguments)
    }
}

override fun onInterstitialAdOpened(instanceId: String) {
    mActivity.runOnUiThread { //back on UI thread...
        mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_OPENED, null)
    }
}

override fun onInterstitialAdClosed(instanceId: String) {
    mActivity.runOnUiThread { //back on UI thread...
        mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_CLOSED, null)
    }
}

override fun onInterstitialAdShowSucceeded(instanceId: String) {
    mActivity.runOnUiThread { //back on UI thread...
        mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_SHOW_SUCCEEDED, null)
    }
}

override fun onInterstitialAdShowFailed(instanceId: String, ironSourceError: IronSourceError) {
    mActivity.runOnUiThread { //back on UI thread...
        val arguments = hashMapOf<String, Any>()
        arguments["errorCode"] = ironSourceError.errorCode
        arguments["errorMessage"] = ironSourceError.errorMessage
        mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_SHOW_FAILED, arguments)
    }
}


  // --------- IronSource Rewarded Video Listener ---------
override fun onRewardedVideoAdOpened(instanceId: String) {
    // called when the video is opened
    mActivity.runOnUiThread { //back on UI thread...
        val arguments = hashMapOf<String, Any>()
        arguments["instanceId"] = instanceId
        mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_OPENED, arguments)
    }
}

override fun onRewardedVideoAdClosed(instanceId: String) {
    mActivity.runOnUiThread { //back on UI thread...
        val arguments = hashMapOf<String, Any>()
        arguments["instanceId"] = instanceId
        mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_CLOSED, arguments)
    }
}

override fun onRewardedVideoAvailabilityChanged(instanceId: String, available: Boolean) {
    // called when the video availability has changed
    mActivity.runOnUiThread { //back on UI thread...
        val arguments = hashMapOf<String, Any>()
        arguments["instanceId"] = instanceId
        arguments["available"] = available
        mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AVAILABILITY_CHANGED, arguments)
    }
}

override fun onRewardedVideoAdStarted(instanceId: String) {
    mActivity.runOnUiThread { //back on UI thread...
        val arguments = hashMapOf<String, Any>()
        arguments["instanceId"] = instanceId
        mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_STARTED, arguments)
    }
}

override fun onRewardedVideoAdEnded(instanceId: String) {
    mActivity.runOnUiThread { //back on UI thread...
        val arguments = hashMapOf<String, Any>()
        arguments["instanceId"] = instanceId
        mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_ENDED, arguments)
    }
}

override fun onRewardedVideoAdRewarded(instanceId: String, placement: Placement) {
    mActivity.runOnUiThread {
        val arguments = hashMapOf<String, Any>()
        arguments["instanceId"] = instanceId
        arguments["placementId"] = placement.placementId
        arguments["placementName"] = placement.placementName
        arguments["rewardAmount"] = placement.rewardAmount
        arguments["rewardName"] = placement.rewardName
        mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_REWARDED, arguments)
    }
}

override fun onRewardedVideoAdShowFailed(instanceId: String, ironSourceError: IronSourceError) {
    mActivity.runOnUiThread {
        val arguments = hashMapOf<String, Any>()
        arguments["instanceId"] = instanceId
        arguments["errorCode"] = ironSourceError.errorCode
        arguments["errorMessage"] = ironSourceError.errorMessage
        mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_SHOW_FAILED, arguments)
    }
}

override fun onRewardedVideoAdClicked(instanceId: String, placement: Placement) {
    mActivity.runOnUiThread {
        val arguments = hashMapOf<String, Any>()
        arguments["instanceId"] = instanceId
        arguments["placementId"] = placement.placementId
        arguments["placementName"] = placement.placementName
        arguments["rewardAmount"] = placement.rewardAmount
        arguments["rewardName"] = placement.rewardName
        mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_CLICKED, arguments)
    }
}

 // --------- IronSource Offerwall Listener ---------
  override fun onOfferwallAvailable() {
    mActivity.runOnUiThread { //back on UI thread...
      mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_AVAILABLE, null)
    }
  }

  override fun onOfferwallOpened() {
    mActivity.runOnUiThread { //back on UI thread...
      mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_OPENED, null)
    }
  }

  override fun onOfferwallShowFailed(error: IronSourceError) {
    mActivity.runOnUiThread {
      val arguments = hashMapOf<String, Any>(
        "errorCode" to error.errorCode,
        "errorMessage" to error.errorMessage
      )
      mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_SHOW_FAILED, arguments)
    }
  }

  override fun onOfferwallAdCredited(credits: Int, totalCredits: Int, totalCreditsFlag: Boolean): Boolean {
    mActivity.runOnUiThread {
      val arguments = hashMapOf<String, Any>(
        "credits" to credits,
        "totalCredits" to totalCredits,
        "totalCreditsFlag" to totalCreditsFlag
      )
      mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_AD_CREDITED, arguments)
    }
    return false
  }

  override fun onOfferwallClosed() {
    mActivity.runOnUiThread { //back on UI thread...
      mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_CLOSED, null)
    }
  }

  override fun onOfferwallAvailable(available: Boolean) {
    mActivity.runOnUiThread { //back on UI thread...
      mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_AVAILABLE, available)
    }
  }

  override fun onOfferwallOpened(placement: Placement?) {
    mActivity.runOnUiThread { //back on UI thread...
      mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_OPENED, null)
    }
  }

  override fun onOfferwallClosed() {
    mActivity.runOnUiThread { //back on UI thread...
      mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_CLOSED, null)
    }
  }

  override fun onOfferwallShowFailed(placement: Placement?, error: IronSourceError?) {
    mActivity.runOnUiThread {
      val arguments = hashMapOf<String, Any>(
        "errorCode" to error?.errorCode,
        "errorMessage" to error?.errorMessage
      )
      mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_SHOW_FAILED, arguments)
    }
  }

  override fun onOfferwallAdCredited(credits: Int, totalCredits: Int, totalCreditsFlag: Boolean, activity: Activity?): Boolean {
    mActivity.runOnUiThread {
      val arguments = hashMapOf<String, Any>(
        "credits" to credits,
        "totalCredits" to totalCredits,
        "totalCreditsFlag" to totalCreditsFlag
      )
      mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_AD_CREDITED, arguments)
    }
    return false
  }

  override fun onGetOfferwallCreditsFailed(error: IronSourceError?) {
    mActivity.runOnUiThread {
      val arguments = hashMapOf<String, Any>(
        "errorCode" to error?.errorCode,
        "errorMessage" to error?.errorMessage
      )
      mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_CREDITS_FAILED, arguments)
    }
  }

  // override fun onOfferwallAvailable(placement: Placement?) {
  //   mActivity.runOnUiThread { //back on UI thread...
  //     mChannel
  //   }
  // }
}
