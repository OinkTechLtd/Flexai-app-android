# FLEXAI — Futuristic WebView App 🌌

[English version below](#english-version)

**FLEXAI** — это высокотехнологичное гибридное мобильное приложение на платформе Android (Kotlin, Jetpack Compose), разработанное для отображения и кэширования веб-платформы ИИ: **`flexai-ru.lovable.app`** и **`flexai-ru.base44.app`**. Приложение сочетает адаптивный веб-интерфейс с мощным нативным функционалом здоровья (Health Check) и полной поддержкой автономной работы.

---

## 🎨 Дизайн и Визуальный Стиль

* **Заставка GIGA** — Минималистичная, высококонтрастная стартовая обложка приложения, выполненная в стиле сурового премиального минимализма GIGA: пульсирующий неоновый элемент эмблемы «F», плавающие скопления виртуальных частиц и статус подключения.
* **Футуристический интерфейс (Cyberpunk Dark)** — Экраны приложения выполнены на абсолютно черном фоне глубокого космоса с неоновыми зелеными (`#00FF9D`), фиолетовыми и бирюзовыми акцентами, создавая ощущение терминала будущего.
* **Плавные интерактивные анимации** — Тактильные виброотклики на действия, анимированные слайдеры интервалов времени и затухающие уведомления.

---

## ⚡ Ключевые возможности

### 1. Интеллектуальный WebView-клиент
* Автоматическое переключение между основным хостом (`flexai-ru.lovable.app`) и резервным зеркалом (`flexai-ru.base44.app`).
* Сохранение состояния истории и защита от перезагрузки при поворотах экрана (`configChanges`).
* Полная поддержка JavaScript, LocalStorage, WebGL и баз данных Service Workers.

### 2. Офлайн-режим и ручное клонирование страниц (Offline Saver)
* **Автоматический Кеш**: WebView настраивается на `LOAD_CACHE_ELSE_NETWORK` при обрыве сети.
* **Принудительное сохранение страниц**: Кнопка "Скачать страницу" (иконка звезды) внедряет JavaScript-скрипт, сериализует DOM-структуру текущей страницы, извлекает её заголовок и URL-адрес и надёжно архивирует её в локальную базу данных **Room (SQLite)**.
* **Офлайн Каталог**: Встроенное меню со списком сохраненных документов позволяет открывать их из локальной СУБД в любой момент без интернета.

### 3. Напоминания о здоровье и отдыхе (Sleep & Rest Push Nodes)
* **Напоминание о Сне**: Интуитивный селектор времени сна (например, 22:00) будит нативный фоновый ресивер `AlarmManager` для отправки пуш-уведомления с призывом дать глазам отдохнуть.
* **Таймеры Отдыха**: Установите периодический интервал отдыха (от 15 до 120 минут) для физической разминки.
* **Имитатор пуш-уведомлений**: Интегрированная панель ТС («Тест Системы») позволяет в один клик симулировать отправку пушей прямо сейчас.

---

## 🛠️ Архитектура Проекта

Приложение спроектировано по архитектурному паттерну **MVVM + Clean Repository Pattern**:
* **`SavedPage` / `SavedPageDao` / `AppDatabase`**: Полноценная экосистема баз данных Room для локального хранения скачанных HTML страниц.
* **`SavedPageRepository`**: Слой абстракции данных.
* **`NotificationReceiver` & `NotificationHelper`**: Оркестратор фоновых будильников Android и системных каналов напоминаний.
* **`FlexViewModel`**: Реактивное управление состояниями UI, отслеживание подключения к сети через `ConnectivityManager` и управление будильниками.

---

## 🚀 Инструкция по сборке

1. Установите **Android Studio (Ladybug или новее)**.
2. Склонируйте репозиторий на локальную машину.
3. Откройте проект в Android Studio и дайте Gradle выполнить синхронизацию зависимостей.
4. Соберите приложение с помощью задачи:
   ```bash
   gradle assembleDebug
   ```
5. Установите сгенерированный APK файлы на ваш смартфон или эмулятор.

---

<a name="english-version"></a>

# FLEXAI — Futuristic WebView App 🌌

**FLEXAI** is a high-fidelity hybrid mobile client for Android (Kotlin, Jetpack Compose) built to display and offline-cache the cutting-edge AI web hub: **`flexai-ru.lovable.app`** and **`flexai-ru.base44.app`**.

---

## 🎨 Design & Aesthetic Identity

* **GIGA Cover Splash** — A highly polished minimalist launch screen echoing the premium, high-contrast dark style of GIGA. Contains a breathing cyber logo core, rotating orbital rings, and dynamic grid overlays.
* **Cyberpunk Dark Aesthetic** — Pristine Obsidian Blacks paired with electric matrix greens (`#00FF9D`), synth viloets, and cyan highlights.
* **Fluid UI Motion** — Embedded spring micro-animations, physical click vibrations, and elegant slide-up settings decks.

---

## ⚡ Core Features

### 1. Robust Smart WebView
* Seamless toggling between primary (`flexai-ru.lovable.app`) and secondary backup (`flexai-ru.base44.app`) hosts.
* Hardware-accelerated rendering with full support for DOM Storage, JavaScript, and custom User Agents.
* Strict state preservation on configuration changes (reorientation bypass).

### 2. Authentic Offline Saver (Room Caching)
* **HTTP Cache fallback**: Activates `LOAD_CACHE_ELSE_NETWORK` under web dropouts.
* **HTML Dom Extractor**: The float FAB captures live page details via customized JS evaluation, saving titles and full HTML serialized strings into a local **Room DB**.
* **Saved Catalog**: An offline browser panel with deletion controls to access fully isolated pages anywhere.

### 3. Sleep & Rest Alarm Reminders (Active Push Nodes)
* **Sleep Reminders**: Native low-level background alarms scheduled via `AlarmManager` to cue eyes-down rest.
* **Periodic Break Timers**: Set repeating rest triggers (15 to 120 minutes) to foster continuous well-being.
* **Push Testing Center (TC)**: Instant triggers allowing users to test Sleep, Rest, and simulated site promos directly with a single click.

---

## 🛠️ Build and Complication

* Requirements: **JDK 11 / Android Studio Ladybug+**
* To compile an APK locally, run:
  ```bash
  gradle assembleDebug
  ```
