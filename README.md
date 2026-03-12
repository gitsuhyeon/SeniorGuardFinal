# Fall Detection Monitoring System

An **Android-based fall detection and guardian notification system** designed to monitor elderly individuals and provide real-time alerts when a fall is detected.

The system continuously observes motion through a mobile device camera and sends an alert with a recorded video clip to a guardian when a fall event occurs.

---

# Overview

Falls are one of the most serious risks for elderly individuals living alone.
This project implements a **mobile-based monitoring system** that detects fall events and notifies a guardian in real time.

The system is composed of two application modes:

* **Monitoring Mode** – installed on the elderly person's device
* **Guardian Mode** – installed on the guardian's smartphone

---

# System Architecture

Monitoring Device (Android)
→ Motion Monitoring
→ FastAPI Server (AI Inference)
→ Firebase Cloud Messaging (FCM)
→ Guardian Device Notification

The backend server runs AI inference using trained PyTorch models to determine whether a fall has occurred.

---

# Key Features

## Monitoring Mode (Elderly Device)

* Continuous camera-based motion monitoring
* Fall detection event trigger
* Automatic recording of **15 seconds after fall detection**
* Sends motion data to backend server for AI analysis

---

## Guardian Mode

* Real-time **push notifications via Firebase Cloud Messaging**
* Receives fall alerts immediately
* Displays **15-second video clip after fall detection**
* Provides a history of fall events

---

# Notification Flow

1. Monitoring device observes user motion.
2. Motion data is sent to the backend server.
3. AI models analyze the motion sequence.
4. If a fall is detected:

   * A push notification is sent to the guardian
   * A 15-second video clip is delivered.

---

# AI Model

The fall detection system uses **PyTorch-based sequence models** to analyze motion patterns.

### Model Architecture

Input Motion Sequence
→ Bidirectional GRU
→ Attention Layer
→ Fully Connected Layers
→ Binary Classification (Fall / Normal)

### Model Files

* `best_gru_model.pth`
* `best_impact_model.pth`

These models analyze motion sequences to detect abnormal posture transitions and impact events.

---

# Tech Stack

### Mobile

* Kotlin
* Android Jetpack
* Camera API
* Firebase Cloud Messaging (FCM)

### Backend

* Python
* FastAPI
* PyTorch

### Machine Learning

* PyTorch
* GRU-based Sequence Model

---

# Project Structure

```
fall-detection-system
│
├ android-app
│
├ docs
│   ├ architecture.png
│   └ demo.gif
│
├ ai-model
│   └ model_description.md
│
└ README.md
```

---

# My Contributions

* Designed Android application architecture
* Implemented **Monitoring Mode and Guardian Mode**
* Integrated **Firebase Cloud Messaging for real-time notifications**
* Implemented camera-based motion monitoring
* Implemented fall event handling and UI workflow

---

# Future Improvements

* User authentication system
* Multi-guardian support
* Cloud video storage
* Improved fall detection accuracy
