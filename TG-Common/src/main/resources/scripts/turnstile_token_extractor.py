from undetected_chromedriver import Chrome
import time

driver = Chrome()
driver.get("https://snapinsta.to/ko")
time.sleep(10)  # Turnstile 대기
token = driver.execute_script("return document.querySelector('input[name=\"cf-turnstile-response\"]').value")
print(token)
driver.quit()
