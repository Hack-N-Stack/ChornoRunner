# 🏃 ChornoRunner

**ChornoRunner** is a 3D endless-runner game developed in **Java** as a collaborative group project. The game places the player in a volcanic environment where they must keep moving, avoid obstacles, collect coins, and survive for as long as possible.

The project combines 3D models, character animations, sound effects, user-interface elements, player controls, and dynamically managed path segments to create an interactive runner-game experience.

---

## 🎮 Game Overview

ChornoRunner is inspired by the endless-runner genre. The player moves through a dangerous volcanic environment while reacting to obstacles and collecting items along the way.

The game includes animated player actions such as running, jumping, sliding, and death, together with environmental models, sound effects, menus, and gameplay management systems.

---

## ✨ Features

- 3D endless-runner gameplay
- Animated player character
- Running animation
- Jumping mechanics and animation
- Sliding mechanics and animation
- Death animation and game-over handling
- Dynamic path and world management
- Obstacle-based gameplay
- Coin collection system
- Volcanic 3D environment
- Background gameplay music
- Running, jumping, coin, and death sound effects
- Main menu interface
- Loading screen
- Play and Quit controls
- Volume controls
- Game configuration and data management
- Modular Java package structure

---

## 🕹️ Player Actions

The player can perform different actions during gameplay, including:

- **Run** — continuously move through the game environment
- **Jump** — avoid obstacles by jumping
- **Slide** — pass underneath suitable obstacles
- **Collect Coins** — collect coins encountered during the run
- **Avoid Obstacles** — survive by reacting to obstacles along the path

---

## 🛠️ Technologies Used

- **Java**
- **Java 3D Game Development**
- **Gradle**
- **IntelliJ IDEA**
- **Git**
- **GitHub**
- **GLB / GLTF 3D Models**
- **WAV Audio Assets**

---

## 📁 Project Structure

```text
ChornoRunner/
│
├── assets/
│   ├── Animations/
│   │   ├── death.glb
│   │   ├── jump.glb
│   │   ├── run.glb
│   │   └── slide.glb
│   │
│   ├── Interface/
│   │   ├── loading screens
│   │   ├── menu buttons
│   │   └── volume controls
│   │
│   ├── Models/
│   │   ├── player model
│   │   ├── environment models
│   │   ├── coin model
│   │   └── obstacle/path models
│   │
│   └── Sound/
│       ├── gameplay music
│       ├── running sound
│       ├── jumping sound
│       ├── coin sound
│       └── death sound
│
├── src/main/java/com/chronorunner/
│   ├── Main.java
│   ├── AssetPaths.java
│   ├── GameConfig.java
│   ├── GameMode.java
│   │
│   ├── audio/
│   │   └── SoundManager.java
│   │
│   ├── data/
│   │   └── GameData.java
│   │
│   ├── player/
│   │   └── PlayerController.java
│   │
│   ├── ui/
│   │   ├── LoadingScreen.java
│   │   └── MenuManager.java
│   │
│   ├── util/
│   │   └── SceneUtil.java
│   │
│   └── world/
│       ├── PathSegment.java
│       ├── SegmentType.java
│       └── WorldManager.java
│
├── gradle/
├── .gitignore
├── build.gradle
├── gradlew
├── gradlew.bat
├── settings.gradle
└── README.md
```

---

## 🧩 Main Components

### Player Controller

`PlayerController.java` manages the player's movement and gameplay actions, including the character's running, jumping, and sliding behaviour.

### World Management

`WorldManager.java` manages the game environment and the generation/management of path segments used during gameplay.

### Path System

`PathSegment.java` and `SegmentType.java` help organize the different sections of the runner's path and environment.

### Sound System

`SoundManager.java` manages the game's music and sound effects, including running, jumping, coin collection, and death sounds.

### User Interface

`MenuManager.java` and `LoadingScreen.java` manage the main interface and loading experience of the game.

### Game Data

`GameData.java` is responsible for managing gameplay-related data used by the application.

---

## 🚀 Running the Project

### Prerequisites

Before running the project, make sure you have:

- Java/JDK installed
- IntelliJ IDEA installed
- Git installed if you want to clone the repository

### Clone the Repository

```bash
git clone https://github.com/Hack-N-Stack/ChornoRunner.git
```

Open the cloned project in **IntelliJ IDEA**.

### Important: Working Directory

The game's `assets` directory must be accessible from the project's working directory.

In IntelliJ IDEA, make sure the working directory points to the project root containing:

```text
assets/
src/
build.gradle
```

If the working directory is incorrect, the game may display an error indicating that the assets folder could not be found.

### Run

Allow IntelliJ/Gradle to load the required project dependencies.

Then run:

```text
src/main/java/com/chronorunner/Main.java
```

or use the **Run ▶** button in IntelliJ IDEA.

---

## 🤝 Contributors

### Khansa Aimen

GitHub: `KhansaAimen`

### Areej Maryam

GitHub: `areej-builds`

ChronoRunner was developed collaboratively by **Khansa Aimen and Areej Maryam**. Both contributors worked jointly on the design, development, gameplay implementation, integration, and overall completion of the project.

---

## 🌿 Development Workflow

The project uses Git and GitHub for version control and collaboration.

Development work can be organized through feature branches before being merged into the stable `main` branch.

```text
main
├── feature/khansa-...
└── feature/areej-...
```

This approach allows individual changes to be tracked while maintaining a stable main version of the game.

---

## 📌 Future Improvements

Possible future improvements include:

- Additional obstacle types
- More environment variations
- Improved scoring system
- Difficulty progression
- Additional player animations
- Power-ups
- High-score tracking
- Improved collision mechanics
- Additional levels or environments
- Enhanced menus and settings
- Performance optimizations

---

## 📄 Project Purpose

ChornoRunner was developed as a collaborative programming project to apply concepts of Java development, object-oriented programming, 3D game development, asset integration, event handling, user-interface design, version control, and collaborative software development.