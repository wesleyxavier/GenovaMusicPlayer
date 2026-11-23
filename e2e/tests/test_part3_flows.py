import os
import time
import pytest
from appium.webdriver.common.appiumby import AppiumBy


def _save_screenshot(driver, screen_name: str) -> None:
    out_dir = os.path.join("artifacts", "screenshots", "part3")
    os.makedirs(out_dir, exist_ok=True)
    driver.save_screenshot(os.path.join(out_dir, f"{screen_name}.png"))


def test_part3_library_chip_and_playlists_ui(driver):
    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Aba Músicas").click()
    chip_now_playing = driver.find_element(
        AppiumBy.ACCESSIBILITY_ID, "Abrir tela Reproduzindo Agora"
    )
    assert chip_now_playing.is_displayed()
    _save_screenshot(driver, "biblioteca_chip_tocando_agora")

    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Aba Playlists").click()
    assert driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/et_playlists_search"
    ).is_displayed()
    assert driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/btn_playlists_sort"
    ).is_displayed()
    assert driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/btn_playlists_toggle_view"
    ).is_displayed()
    assert driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/card_smart_most_played"
    ).is_displayed()
    assert driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/card_smart_recently_added"
    ).is_displayed()
    _save_screenshot(driver, "colecao_de_playlists")


def test_part3_create_playlist_and_detail_controls(driver):
    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Aba Playlists").click()
    driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/btn_create_playlist"
    ).click()

    playlist_name = f"E2E Parte 3 {int(time.time()) % 10000}"
    name_input = driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/et_create_playlist_name"
    )
    name_input.send_keys(playlist_name)
    _save_screenshot(driver, "criar_nova_playlist")

    driver.find_element(AppiumBy.ID, "android:id/button1").click()
    driver.find_element(
        AppiumBy.ANDROID_UIAUTOMATOR, f'new UiSelector().text("{playlist_name}")'
    ).click()

    assert driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/btn_playlist_add_tracks"
    ).is_displayed()
    assert driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/btn_playlist_manual_order"
    ).is_displayed()
    assert driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/btn_playlist_add_to_queue"
    ).is_displayed()
    _save_screenshot(driver, "detalhes_da_playlist")

    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Voltar").click()


def test_part3_playback_miniplayer_and_queue_if_tracks_available(driver):
    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Aba Músicas").click()
    rows = driver.find_elements(
        AppiumBy.ID, "com.genova.musicplayer:id/tv_track_title"
    )
    if not rows:
        pytest.skip("Nenhuma faixa indexada no dispositivo para testar mini-player e fila.")

    rows[0].click()
    _save_screenshot(driver, "tocando_agora")

    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Fila a Seguir").click()
    assert driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/tv_queue_origin"
    ).is_displayed()
    assert driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/btn_queue_add"
    ).is_displayed()
    _save_screenshot(driver, "fila_a_seguir")

    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Recolher Fila a Seguir").click()
    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Voltar").click()

    mini_player = driver.find_element(
        AppiumBy.ID, "com.genova.musicplayer:id/mini_player_container"
    )
    assert mini_player.is_displayed()
    _save_screenshot(driver, "biblioteca_com_mini_player_ativo")
