WebApp отправляет initData на backend:

const initData = Telegram.WebApp.initData;
fetch("https://app/auth/telegram", {
method: "POST",
headers: { "Content-Type": "application/json" },
body: JSON.stringify({ initData })
});