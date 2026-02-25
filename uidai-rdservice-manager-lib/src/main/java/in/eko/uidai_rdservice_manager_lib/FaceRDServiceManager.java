package in.eko.uidai_rdservice_manager_lib;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.net.Uri;
import androidx.annotation.NonNull;
import static android.app.Activity.RESULT_OK;

/**
 * Helper class to capture face data using UIDAI Face RD Service
 */
public class FaceRDServiceManager {

    private static final String TAG = "FaceRDServiceManager";
    private static final String FACE_RD_PACKAGE = "in.gov.uidai.facerd";

    private final RDServiceEvents mRDEvent;
    private final Activity activity;
    private final Context context;

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
     * Handle the result from Face RD Service capture intent and send response back to UI / WebView
     */
	public void onRDServiceCaptureIntentResponse(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "handleActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode + ", data: " + (data != null ? "present" : "null"));

        if (requestCode != RD_SERVICE_RESPONSE_FACE) {
            Log.d(TAG, "Request code doesn't match. Expected: " + RD_SERVICE_RESPONSE_FACE + ", Got: " + requestCode);
            return;
        }

        if (resultCode == Activity.RESULT_OK && data != null) {
            String pidData = data.getStringExtra("response");
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
