import os
import pytest
from appium import webdriver
from appium.options.android import UiAutomator2Options


def _build_options() -> UiAutomator2Options:
    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.automation_name = "UiAutomator2"
    options.app_package = os.getenv("APP_PACKAGE", "com.genova.musicplayer")
    options.app_activity = os.getenv("APP_ACTIVITY", "com.genova.musicplayer.ui.LibraryActivity")
    options.no_reset = True
    options.set_capability("appium:forceAppLaunch", True)
    options.set_capability(
        "appium:remoteAdbHost",
        os.getenv("ANDROID_ADB_SERVER_ADDRESS", "host.docker.internal"),
    )
    options.set_capability(
        "appium:adbPort",
        int(os.getenv("ANDROID_ADB_SERVER_PORT", "5037")),
    )
    options.set_capability("appium:uiautomator2ServerInstallTimeout", 60000)

    apk_path = os.getenv("APK_PATH", "").strip()
    if apk_path and os.path.exists(apk_path):
        options.app = apk_path
    return options


@pytest.fixture(scope="session")
def driver():
    server_url = os.getenv("APPIUM_SERVER_URL", "http://appium:4723")
    app_package = os.getenv("APP_PACKAGE", "com.genova.musicplayer")
    drv = webdriver.Remote(server_url, options=_build_options())
    drv.implicitly_wait(5)
    drv.terminate_app(app_package)
    drv.activate_app(app_package)
    yield drv
    drv.quit()
