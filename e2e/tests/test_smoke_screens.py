import os
from appium.webdriver.common.appiumby import AppiumBy


def _save_screenshot(driver, scenario: str, screen_name: str) -> None:
    out_dir = os.path.join("artifacts", "screenshots", scenario)
    os.makedirs(out_dir, exist_ok=True)
    driver.save_screenshot(os.path.join(out_dir, f"{screen_name}.png"))


def test_smoke_navigation_and_screenshots(driver):
    _save_screenshot(driver, "smoke", "biblioteca_busca")

    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Aba Playlists").click()
    _save_screenshot(driver, "smoke", "playlists")

    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Aba Pastas").click()
    _save_screenshot(driver, "smoke", "gerenciador_de_pastas")

    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Aba Músicas").click()
