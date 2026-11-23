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

    apk_path = os.getenv("APK_PATH")
    if apk_path and os.path.exists(apk_path):
        options.app = apk_path
    return options


@pytest.fixture(scope="session")
def driver():
    server_url = os.getenv("APPIUM_SERVER_URL", "http://appium:4723")
    drv = webdriver.Remote(server_url, options=_build_options())
    drv.implicitly_wait(5)
    yield drv
    drv.quit()
