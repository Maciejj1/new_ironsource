package com.itlight.new_flutter_ironsource_x

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.IronSource.AD_UNIT
import com.ironsource.mediationsdk.IronSourceObject
import com.ironsource.mediationsdk.IntegrationHelper
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.model.Placement
import com.ironsource.mediationsdk.sdk.InterstitialListener
import com.ironsource.mediationsdk.sdk.OfferwallListener
import com.ironsource.mediationsdk.sdk.RewardedInterstitialListener
import com.ironsource.mediationsdk.sdk.RewardedVideoListener
import com.ironsource.mediationsdk.sdk.SegmentListener
import com.ironsource.mediationsdk.sdk.SuperRewardedVideoListener

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
class FlutterIronsource_xPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, InterstitialListener, RewardedVideoListener, OfferwallListener {
    private lateinit var mActivity : Activity
    private lateinit var mChannel : MethodChannel
    private lateinit var messenger: BinaryMessenger
    private lateinit var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding

    val TAG = "IronsourcePlugin"
    var APP_KEY = ""
    lateinit var mPlacement: Placement
    val FALLBACK_USER_ID = "userId"

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
        IronSourceConsts.INIT -> {
            val appKey = call.argument<String>("appKey")
            val gdprConsent = call.argument<Boolean>("gdprConsent") ?: false
            val ccpaConsent = call.argument<Boolean>("ccpaConsent") ?: false
            initialize(appKey, gdprConsent, ccpaConsent)
            result.success(null)
        }
        IronSourceConsts.LOAD_INTERSTITIAL -> {
            IronSource.loadInterstitial()
            result.success(null)
        }
        IronSourceConsts.SHOW_INTERSTITIAL -> {
            IronSource.showInterstitial()
            result.success(null)
        }
        IronSourceConsts.IS_INTERSTITIAL_READY -> {
            result.success(IronSource.isInterstitialReady())
        }
        IronSourceConsts.IS_REWARDED_VIDEO_AVAILABLE -> {
            result.success(IronSource.isRewardedVideoAvailable())
        }
        IronSourceConsts.IS_OFFERWALL_AVAILABLE -> {
            result.success(IronSource.isOfferwallAvailable())
        }
        IronSourceConsts.SHOW_OFFERWALL -> {
            IronSource.showOfferwall()
            result.success(null)
        }
        IronSourceConsts.SHOW_REWARDED_VIDEO -> {
            IronSource.showRewardedVideo()
            result.success(null)
        }
        "validateIntegration" -> {
            IntegrationHelper.validateIntegration(mActivity)
            result.success(null)
        }
        "setUserId" -> {
            IronSource.setUserId(call.argument<String>("userId"))
            result.success(null)
        }
        "getAdvertiserId" -> {
            result.success(IronSource.getAdvertiserId(mActivity))
        }
        "activityResumed" -> {
            IronSource.onResume(mActivity)
            result.success(null)
        }
        "activityPaused" -> {
            IronSource.onPause(mActivity)
            result.success(null)
        }
        "shouldTrackNetworkState" -> {
            if (call.hasArgument("state")) {
                call.argument<Boolean>("state")?.let { IronSource.shouldTrackNetworkState(mActivity, it) }
            }
            result.success(null)
        }
        else -> {
            result.notImplemented()
        }
    }
}

fun initialize(appKey: String, gdprConsent: Boolean, ccpaConsent: Boolean) {
    IronSource.setInterstitialListener(this)
    IronSource.setRewardedVideoListener(this)
    IronSource.setOfferwallListener(this)
    IronSource.setConsent(gdprConsent)
    IronSource.addISDemandSourceListener(this)
    if (ccpaConsent) {
        IronSource.updateMetaData(IronSourceConstants.IS_DO_NOT_SELL, "false")
    } else {
        IronSource.updateMetaData(IronSourceConstants.IS_DO_NOT_SELL, "true")
    }

    val config = IronSourceConfiguration.Builder(mActivity)
        .setInterstitialAdapters(listOf(IronSourceAdapter.GENERAL))
        .setRewardedVideoAdapters(listOf(IronSourceAdapter.GENERAL))
        .setOfferwallAdapters(listOf(IronSourceAdapter.GENERAL))
        .build()

    IronSource.initWithAppKey(mActivity, appKey, config)
}
// Interstitial Listener
  override fun onInterstitialClick() {
    mActivity.runOnUiThread { //back on UI thread...
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_CLICKED, null)
    }
  }

  override fun onInterstitialAdReady() {
    mActivity.runOnUiThread { //back on UI thread...
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_READY, null)
    }
  }

  override fun onInterstitialLoadFailed(ironSourceError: IronSourceError) {
    mActivity.runOnUiThread { //back on UI thread...
      val arguments = HashMap<String, Any>()
      arguments["errorCode"] = ironSourceError.errorCode
      arguments["errorMessage"] = ironSourceError.errorMessage
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_LOAD_FAILED, arguments)
    }
  }

  override fun onInterstitialOpen() {
    mActivity.runOnUiThread { //back on UI thread...
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_OPENED, null)
    }
  }

  override fun onInterstitialClose() {
    mActivity.runOnUiThread { //back on UI thread...
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_CLOSED, null)
    }
  }

  override fun onInterstitialShowSucceeded() {
    mActivity.runOnUiThread { //back on UI thread...
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_SHOW_SUCCEEDED, null)
    }
  }

  override fun onInterstitialShowFailed(ironSourceError: IronSourceError) {
    mActivity.runOnUiThread { //back on UI thread...
      val arguments = HashMap<String, Any>()
      arguments["errorCode"] = ironSourceError.errorCode
      arguments["errorMessage"] = ironSourceError.errorMessage
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_SHOW_FAILED, arguments)
    }
  }
// --------- IronSource Rewarded Video Listener ---------
override fun onRewardedVideoAdOpened(instanceId: String) {
// called when the video is opened
mActivity.runOnUiThread { //back on UI thread...
mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_OPENED, null)
}
}

override fun onRewardedVideoAdClosed(instanceId: String) {
mActivity.runOnUiThread { //back on UI thread...
mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_CLOSED, null)
}
}

override fun onRewardedVideoAvailabilityChanged(instanceId: String, available: Boolean) {
// called when the video availbility has changed
mActivity.runOnUiThread { //back on UI thread...
mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AVAILABILITY_CHANGED, available)
}
}

override fun onRewardedVideoAdStarted(instanceId: String) {
mActivity.runOnUiThread { //back on UI thread...
mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_STARTED, null)
}
}

override fun onRewardedVideoAdEnded(instanceId: String) {
mActivity.runOnUiThread { //back on UI thread...
mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_ENDED, null)
}
}

override fun onRewardedVideoAdRewarded(instanceId: String, placement: Placement) {
mActivity.runOnUiThread {
val arguments = HashMap<String, Any>()
arguments["placementId"] = placement.placementId
arguments["placementName"] = placement.placementName
arguments["rewardAmount"] = placement.rewardAmount
arguments["rewardName"] = placement.rewardName
mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_REWARDED, arguments)
}
}

override fun onRewardedVideoAdShowFailed(instanceId: String, error: IronSourceError) {
mActivity.runOnUiThread {
val arguments = HashMap<String, Any>()
arguments["errorCode"] = error.errorCode
arguments["errorMessage"] = error.errorMessage
mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_SHOW_FAILED, arguments)
}
}

override fun onRewardedVideoAdClicked(instanceId: String, placement: Placement) {
mActivity.runOnUiThread {
val arguments = HashMap<String, Any>()
arguments["placementId"] = placement.placementId
arguments["placementName"] = placement.placementName
arguments["rewardAmount"] = placement.rewardAmount
arguments["rewardName"] = placement.rewardName
mChannel.invokeMethod(IronSourceConsts.ON_REWARDED_VIDEO_AD_CLICKED, arguments)
}
}
// --------- IronSource Offerwall Listener ---------
override fun onOfferwallAvailable(isAvailable: Boolean) {
  mActivity.runOnUiThread { //back on UI thread...
    val arguments = HashMap<String, Any>()
    arguments["isAvailable"] = isAvailable
    mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_AVAILABLE, arguments)
  }
}

override fun onOfferwallOpened() {
  mActivity.runOnUiThread { //back on UI thread...
    mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_OPENED, null)
  }
}

override fun onOfferwallShowFailed(error: IronSourceError) {
  mActivity.runOnUiThread {
    val arguments = HashMap<String, Any>()
    arguments["errorCode"] = error.errorCode
    arguments["errorMessage"] = error.errorMessage
    mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_SHOW_FAILED, arguments)
  }
}

override fun onOfferwallAdCredited(credits: Int, totalCredits: Int, totalCreditsFlag: Boolean): Boolean {
  mActivity.runOnUiThread {
    val arguments = HashMap<String, Any>()
    arguments["credits"] = credits
    arguments["totalCredits"] = totalCredits
    arguments["totalCreditsFlag"] = totalCreditsFlag
    mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_AD_CREDITED, arguments)
  }
  return false
}

override fun onGetOfferwallCreditsFailed(error: IronSourceError) {
  mActivity.runOnUiThread {
    val arguments = HashMap<String, Any>()
    arguments["errorCode"] = error.errorCode
    arguments["errorMessage"] = error.errorMessage
    mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_CREDITS_FAILED, arguments)
  }
}

override fun onOfferwallClosed() {
  mActivity.runOnUiThread { //back on UI thread...
    mChannel.invokeMethod(IronSourceConsts.ON_OFFERWALL_CLOSED, null)
  }
}




  /*companion object {
    @JvmStatic
    fun registerWith(registrar: PluginRegistry.Registrar) {
      val channel = MethodChannel(registrar.messenger(), IronSourceConsts.MAIN_CHANNEL)
      channel.setMethodCallHandler(FlutterIronsource_xPlugin())
      val interstitialAdChannel = MethodChannel(registrar.messenger(), IronSourceConsts.INTERSTITIAL_CHANNEL)
      registrar.platformViewRegistry().registerViewFactory(IronSourceConsts.BANNER_AD_CHANNEL, IronSourceBanner(registrar.activity(), registrar.messenger()))
  }*/

  /*private companion object Factory {
    fun setup(plugin: FlutterIronsource_xPlugin, binaryMessenger: BinaryMessenger) {
    }
  }*/



 override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    this.flutterPluginBinding = flutterPluginBinding
    this.mChannel = MethodChannel(flutterPluginBinding.binaryMessenger, IronSourceConsts.MAIN_CHANNEL)
    this.mChannel.setMethodCallHandler(this)
    Log.i("DEBUG","Tesst On Attached")
    val interstitialAdChannel = MethodChannel(flutterPluginBinding.binaryMessenger, IronSourceConsts.INTERSTITIAL_CHANNEL)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    this.mChannel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    this.mActivity = binding.activity;
    IronSource.setConsent(true) // Set the user's consent to 'true' for personalized ads.
    IronSource.init(this.mActivity, IronSourceConsts.APP_KEY, IronSource.AD_UNIT.INTERSTITIAL)
    IronSource.setInterstitialListener(this)
    IronSource.loadInterstitial() // Load the interstitial ad.
    Log.i("DEBUG", "Tesst On Activity")
    this.flutterPluginBinding.platformViewRegistry.registerViewFactory(IronSourceConsts.BANNER_AD_CHANNEL, IronSourceBanner(binding.activity, this.flutterPluginBinding.binaryMessenger))
  }

  override fun onDetachedFromActivityForConfigChanges() {
    // Not required for IronSource SDK 7.2.7.
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    // Not required for IronSource SDK 7.2.7.
  }

  override fun onDetachedFromActivity() {
    // Not required for IronSource SDK 7.2.7.
  }

  override fun onInterstitialAdReady() {
    mActivity.runOnUiThread { // Back on the UI thread...
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_READY, null)
    }
  }

  override fun onInterstitialAdLoadFailed(ironSourceError: IronSourceError) {
    mActivity.runOnUiThread {
      val arguments = HashMap<String, Any>()
      arguments["errorCode"] = ironSourceError.errorCode
      arguments["errorMessage"] = ironSourceError.errorMessage
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_LOAD_FAILED, arguments)
    }
  }

  override fun onInterstitialAdOpened() {
    mActivity.runOnUiThread {
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_OPENED, null)
    }
  }

  override fun onInterstitialAdClosed() {
    mActivity.runOnUiThread {
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_CLOSED, null)
    }
  }

  override fun onInterstitialAdShowSucceeded() {
    mActivity.runOnUiThread {
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_SHOW_SUCCEEDED, null)
    }
  }

  override fun onInterstitialAdShowFailed(ironSourceError: IronSourceError) {
    mActivity.runOnUiThread {
      val arguments = HashMap<String, Any>()
      arguments["errorCode"] = ironSourceError.errorCode
      arguments["errorMessage"] = ironSourceError.errorMessage
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_SHOW_FAILED, arguments)
    }
  }

  override fun onInterstitialAdClicked() {
    mActivity.runOnUiThread {
      mChannel.invokeMethod(IronSourceConsts.ON_INTERSTITIAL_AD_CLICKED, null)
    }
  }
}
