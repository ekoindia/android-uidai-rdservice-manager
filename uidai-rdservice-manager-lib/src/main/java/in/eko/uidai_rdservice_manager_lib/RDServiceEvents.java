package in.eko.uidai_rdservice_manager_lib;

import android.content.Intent;

/**
 * An interface to implement RDServiceManager callback functions.
 */
public interface RDServiceEvents {

	/**
	 * An RDService driver is discovered. For each installed driver, this function will be called separately with the current status and package-name of that driver.
	 * @param rdServiceInfo Status response as XML string from the discovered RDService device driver
	 * @param rdServicePackage The discovered RDService device driver package name
	 */
	public void onRDServiceDriverDiscovery(String rdServiceInfo, String rdServicePackage);

	/**
	 * A fingerprint scan data is received from the RDService.
	 * @param pidData The fingerprint scan PID data as XML string
	 */
	public void onRDServiceCaptureResponse(String pidData, String rdServicePackage);

	/**
	 * No installed RDService driver was found.
	 */
	public void onRDServiceDriverNotFound();

	/**
	 * An installed RDService driver did not return a proper status.
	 * @param resultCode The resultCode returned by the RDServiver driver activity
	 * @param data The data returned by the RDServiver driver activity
	 * @param rdServicePackage The package name of the RDService driver
	 */
	public void onRDServiceDriverDiscoveryFailed(int resultCode, Intent data, String rdServicePackage, String reason);

	/**
	 * Captured request sent to an RDService driver failed.
	 * @param resultCode The resultCode returned by the RDServiver driver activity
	 * @param data The data returned by the RDServiver driver activity
	 * @param rdServicePackage The package name of the RDService driver
	 */
	public void onRDServiceCaptureFailed(int resultCode, Intent data, String rdServicePackage);
}
