package in.eko.uidai_rdservice_manager_lib;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class RDServiceManager {

	private static final String TAG = "RDServiceManager";
	private RDServiceEvents mRDEvent;

	private static final int RC_RDSERVICE_DISCOVER_START_INDEX = 8500;
	private static final int RC_RDSERVICE_CAPTURE_START_INDEX = 8300;

	private static final Map<String, Integer> mapRDDriverRCIndex = new HashMap<String, Integer>();
	private static final Map<Integer, String> mapRDDiscoverRC = new HashMap<Integer, String>();
	private static final Map<Integer, String> mapRDCaptureRC = new HashMap<Integer, String>();

	private static final Map<String, String> mapRDDriverWhitelist = new HashMap<String, String>() {
		{
			put("com.secugen.rdservice", "Secugen");
			put("com.scl.rdservice", "Morpho");
			put("com.mantra.rdservice", "Mantra");
			put("com.acpl.registersdk", "Startek FM220");
			put("com.rd.gemalto.com.rdserviceapp", "Gemalto 3M Cogent CSD200");

			put("com.tatvik.bio.tmf20", "Tatvik TMF20");
			put("com.evolute.rdservice", "Evolute");
			put("com.precision.pb510.rdservice", "PB510");
			put("com.mantra.mis100v2.rdservice", "MIS100V2 by Mantra");
			put("com.nextbiometrics.rdservice", "NEXT Biometrics NB-3023");
			put("com.iritech.rdservice", "IriTech IriShield");
		}
	};

	private static final Map<String, String> mapRDDriverBlacklist = new HashMap<String, String>();


	private RDServiceManager(final Builder builder) {
		mRDEvent = builder._rdevent;
		mapRDDriverWhitelist.putAll(builder.mapNewWhitelistedRDDrivers);
		mapRDDriverBlacklist.putAll(builder.mapBlacklistedRDDrivers);
	}


	public static class Builder {
		private RDServiceEvents _rdevent = null;
		private Map<String, String> mapNewWhitelistedRDDrivers = new HashMap<String, String>();
		private Map<String, String> mapBlacklistedRDDrivers = new HashMap<String, String>();

		public Builder(final RDServiceEvents event) {
			_rdevent = event;
		}

		public Builder whitelistRDDrivers(final Map<String, String> mapNewWhitelistedRDDrivers) {
			this.mapNewWhitelistedRDDrivers = mapNewWhitelistedRDDrivers;
			return this;
		}

		public Builder blacklistRDDrivers(final Map<String, String> mapBlacklistedRDDrivers) {
			this.mapBlacklistedRDDrivers = mapBlacklistedRDDrivers;
			return this;
		}

		public RDServiceManager create() {
			if (_rdevent == null) {
				throw new IllegalStateException("First set your Activity that implements RDServiceEvent by calling setRDServiceEventActivity()");
			}
			return new RDServiceManager(this);
		}
	}


	/**
	 * Dispatch onActivityResult here from the implementing Activity.
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i(TAG, "onActivityResult: " + requestCode + ", " + resultCode+ " ~~ " + mapRDDiscoverRC.toString());

		if (mapRDDiscoverRC.containsKey(requestCode)) {
			String rdservice_pkg_name = mapRDDiscoverRC.get(requestCode);
			if (resultCode == RESULT_OK) {
				onRDServiceInfoResponse(data, rdservice_pkg_name);  // RDService Info Received
			} else {
				mRDEvent.onRDServiceDriverDiscoveryFailed(resultCode, data, rdservice_pkg_name, "");    // RDService Info Failed
			}
		}
		else if (mapRDCaptureRC.containsKey(requestCode)) {
			String rdservice_pkg_name = mapRDCaptureRC.get(requestCode);
			if (resultCode == RESULT_OK) {
				onRDServiceCaptureIntentResponse(data, rdservice_pkg_name);  // Fingerprint Captured
			} else {
				mRDEvent.onRDServiceCaptureFailed(resultCode, data, rdservice_pkg_name);    // Fingerprint Capture Failed
			}
		}
	}



	public void discoverRdService() {
		Intent intentServiceList = new Intent("in.gov.uidai.rdservice.fp.INFO");
		List<ResolveInfo> resolveInfoList = ((Activity) mRDEvent).getPackageManager().queryIntentActivities(intentServiceList, 0);

		// String packageNamesStr = "";

		if (resolveInfoList.isEmpty())
		{
			mRDEvent.onRDServiceDriverNotFound();
			return;
		}

		int iInfo = 0;
		for (ResolveInfo resolveInfo :resolveInfoList)
		{
			String _pkg = resolveInfo.activityInfo.packageName;

			if (mapRDDriverWhitelist.containsKey(_pkg) && !mapRDDriverBlacklist.containsKey(_pkg)) {
				try {
					// Assign an index to current RDService driver
					int next_rdservice_index = mapRDDriverRCIndex.size() + 1;
					mapRDDriverRCIndex.put(_pkg, next_rdservice_index);

					// Calculate and map request-code for the current RDService GetInfo Intent
					int next_discover_rc_index = getRDServiceDiscoverRC(next_rdservice_index);
					mapRDDiscoverRC.put(next_discover_rc_index, _pkg);

					// Calculate and map request-code for the current RDService Capture Intent
					int next_capture_rc_index = getRDServiceCaptureRC(next_rdservice_index);
					mapRDCaptureRC.put(next_capture_rc_index, _pkg);

					// Get RD Service Info..
					Intent intentInfo = new Intent("in.gov.uidai.rdservice.fp.INFO");
					intentInfo.setPackage(_pkg);
					((Activity) mRDEvent).startActivityForResult(intentInfo, next_discover_rc_index);

					Log.e(TAG, "RD SERVICE Package Found: (" + next_rdservice_index + ") " + next_discover_rc_index + " ~ " + _pkg + " ~~ " + mapRDDriverRCIndex + " ~ " + mapRDDiscoverRC);
				} catch (Exception e) {
					e.printStackTrace();
					mRDEvent.onRDServiceDriverDiscoveryFailed(0, null, _pkg, e.getMessage());
				}
			} else {
				mRDEvent.onRDServiceDriverDiscoveryFailed(0, null, _pkg, "Package not whitelisted");
			}


			++iInfo;

			// Limit max installed driver discovery count
			if (iInfo > 10) {
				break;
			}
		}
	}


	public void captureRdService(@NonNull String data) {

		try {
			JSONObject jsonData = new JSONObject(data);

			String _package = jsonData.getString("package");
			String _pidopts = jsonData.getString("pidopts");
			// String _url = jsonData.getString("url");

			if (mapRDDriverRCIndex.containsKey(_package)) {
				int capture_rc_index = mapRDDriverRCIndex.get(_package);
				int capture_rc = getRDServiceCaptureRC(capture_rc_index);

				Log.d(TAG, "RDSERVICE BEFORE CAPTURE: pid_options: (" + capture_rc_index + ") " + capture_rc + " ~ " + _pidopts);

				// Capture fingerprint using RD Service
				Intent intentCapture = new Intent("in.gov.uidai.rdservice.fp.CAPTURE");
				intentCapture.setPackage(_package);
				intentCapture.putExtra("PID_OPTIONS", _pidopts);
				((Activity) mRDEvent).startActivityForResult(intentCapture, capture_rc);
			} else {
				Log.d(TAG, "RDSERVICE CAPTURE ERROR: package not found or not whitelisted: " + _package);
				// TODO: Call capture failed function with error string...
			}
		} catch (JSONException | NullPointerException e) {
			e.printStackTrace();
		}
	}


	private void onRDServiceInfoResponse(@NonNull Intent data, @NonNull String rd_service_package) {

		Bundle b = data.getExtras();

		if (b != null)
		{
			// sendWebViewResponse("rdservice_info", b.getString("RD_SERVICE_INFO", "") + "<RD_SERVICE_ANDROID_PACKAGE=\"" + rd_service_package + "\" />");

			Log.d(TAG, "onRDServiceInfoResponse: " + b.getString("RD_SERVICE_INFO", "") + " //// " +  rd_service_package);

			mRDEvent.onRDServiceDriverDiscovery(b.getString("RD_SERVICE_INFO", ""), rd_service_package);

			Log.i(TAG, "onRDServiceInfoResponse: Device Info: \n\n Device = " + b.getString("DEVICE_INFO", "") + "    \n\nRDService = " + b.getString("RD_SERVICE_INFO", ""));
		}
	}



	private void onRDServiceCaptureIntentResponse(@NonNull Intent data, @NonNull String pkg) {

		Bundle b = data.getExtras();

		if (b != null)
		{
			// sendWebViewResponse("rdservice_resp", b.getString("PID_DATA", ""));
			mRDEvent.onRDServiceCaptureResponse(b.getString("PID_DATA", ""));

			Log.i(TAG, "onRDServiceCaptureIntentResponse: Capture Info: \n\n PID-DATA = " + b.getString("PID_DATA", "") + "    \n\nDeviceNotConnected = " + b.getString("DNC", ""));
		}
	}


	private int getRDServiceDiscoverRC(int index)
	{
		return RC_RDSERVICE_DISCOVER_START_INDEX + index;
	}

	private int getRDServiceCaptureRC(int index)
	{
		return RC_RDSERVICE_CAPTURE_START_INDEX + index;
	}

}
