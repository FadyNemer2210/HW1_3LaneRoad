# ⚽ HW1 3-Lane Road Game

An Android mini-game built in Kotlin where the player controls the Arsenal logo and must dodge falling UEFA Champions League trophies.

Because... Arsenal and UCL don't mix 💀

---

## 🎮 Gameplay

- 3-lane football road
- Player can move left and right
- Multiple falling UCL trophies
- Matrix-based movement system (row/lane logic)
- Collision detection
- 3 lives system
- Football icons as life indicators
- Crash vibration feedback
- Arsenal banter on collisions
- Game over popup with restart button

---

## 🕹 Controls

- ⬅ Left arrow → move left
- ➡ Right arrow → move right
- Start Over button → restart after game over

---

## 🧠 Game Logic

The game uses a **matrix/grid movement approach** instead of direct pixel collision logic.

Each obstacle has:

```kotlin
row
lane
