package in.eko.uidai_rdservice_manager_lib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.os.Bundle;
import android.util.Log;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.List;


import static android.app.Activity.RESULT_OK;

/**
 * Helper class to capture face data using UIDAI Face RD Service
 */
public class FaceRDServiceManager {

    private static final String TAG = "FaceRDServiceManager";

    private static final String FACE_RD_PACKAGE = "in.gov.uidai.facerd";
    private static final String FACE_CAPTURE_ACTION = "in.gov.uidai.rdservice.face.CAPTURE";

    private static final int FACE_CAPTURE_REQ = 9001;

    private final RDServiceEvents mRDEvent;
    private final Activity activity;
    private final Context context;

    private String pidOptsXml;

    /* ---------------- CONSTRUCTOR ---------------- */

    private FaceRDServiceManager(@NonNull Builder builder) {
        this.mRDEvent = builder.event;
        this.activity = (Activity) builder.event; // Activity implements RDServiceEvents
        this.context = this.activity;
    }

    /* ---------------- BUILDER ---------------- */

    public static class Builder {
        private final RDServiceEvents event;

        public Builder(@NonNull RDServiceEvents eventActivity) {
            this.event = eventActivity;
        }

        public FaceRDServiceManager create() {
            return new FaceRDServiceManager(this);
        }
    }

	
	private static final int RD_SERVICE_RESPONSE_FACE = 1002;

    /**
     * Send Face RD Service discovery response with XML data
     */
    public void discoverFaceRdService() {

        Log.d(TAG, "discoverFaceRdService: Sending Face RD Service discovery response");
        String faceRdServiceInfo = "<RDService info=\"Aadhar Face Rd Service\" status=\"READY\">\n" +
                "    <Interface id=\"CAPTURE\" path=\"in.gov.uidai.rdservice.face.CAPTURE\"/>\n" +
                "    <Interface id=\"DEVICEINFO\" path=\"in.gov.uidai.rdservice.face.INFO\"/>\n" +
                "</RDService>";

        Log.d(TAG, "discoverFaceRdService: " + faceRdServiceInfo);

        // Send discovery response
        mRDEvent.onRDServiceDriverDiscovery(faceRdServiceInfo, FACE_RD_PACKAGE, true);
    }

    /**
     * capture Face RD Service with given PID options
     */
    public void captureFaceRdService(@NonNull String rd_service_package, @NonNull String pid_options) {

        Log.d(TAG, "captureFaceRdService: Starting Face RD flow" + rd_service_package +  pid_options);

        try {
			Intent intent = new Intent(rd_service_package);
			intent.putExtra("request", pid_options);
            activity.startActivityForResult(intent, RD_SERVICE_RESPONSE_FACE);

		} catch (Exception e) {
			Log.w(TAG, "Face RD app not installed. Redirecting to Play Store.", e);

			// Redirect to Play Store
			try {
				Intent playStoreIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("market://details?id=" + FACE_RD_PACKAGE));
				playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(playStoreIntent);

			} catch (Exception ex) {
				// Play Store not available → open in browser
				Intent browserIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("https://play.google.com/store/apps/details?id=" + FACE_RD_PACKAGE));
				browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(browserIntent);
			}

			// Notify UI / WebView
			mRDEvent.onRDServiceDriverNotFound();
		}
    }


    /**
     * Handle Activity Result for Face RD Service
     */
	public void handleActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "handleActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode + ", data: " + (data != null ? "present" : "null"));

        if (requestCode != RD_SERVICE_RESPONSE_FACE) {
            Log.d(TAG, "Request code doesn't match. Expected: " + RD_SERVICE_RESPONSE_FACE + ", Got: " + requestCode);
            return;
        }

        if (resultCode == Activity.RESULT_OK && data != null) {

            // Log all extras to see what keys are available
            Bundle extras = data.getExtras();
            if (extras != null) {
                Log.d(TAG, "Available extras: " + extras.keySet());
                for (String key : extras.keySet()) {
                    Log.d(TAG, "Key: " + key + ", Value: " + extras.get(key));
                }
            }

            String pidData = data.getStringExtra("PID_DATA");

            if (pidData == null || pidData.trim().isEmpty()) {
                Log.e(TAG, "Face RD returned RESULT_OK but PID_DATA is null or empty");
                Log.e(TAG, "Trying alternative key 'response'...");
                pidData = data.getStringExtra("response");
                
                if (pidData == null || pidData.trim().isEmpty()) {
                    Log.e(TAG, "No PID_DATA found in intent extras");
                    mRDEvent.onRDServiceCaptureFailed(-1, data, FACE_RD_PACKAGE);
                    return;
                }
            }

            Log.d(TAG, "Face PID received successfully: " + pidData.substring(0, Math.min(100, pidData.length())) + "...");
            mRDEvent.onRDServiceCaptureResponse(pidData, FACE_RD_PACKAGE);

        } else {
            Log.e(TAG, "Face RD capture failed or data is null");
            if (data == null) {
                Log.e(TAG, "Intent data is NULL");
            }
            mRDEvent.onRDServiceCaptureFailed(resultCode, data, FACE_RD_PACKAGE);
        }
    }

}
