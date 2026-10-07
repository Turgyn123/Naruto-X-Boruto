# Naruto x Boruto Mod

A Naruto and Boruto mod for Minecraft **1.21.1**. Pick a clan, train your stats, grow your chakra and
learn jutsu, wield the Seven Swordsmen's blades and hunt the shinobi bosses.

Available on **NeoForge**, **Fabric** and **Forge**, built from one shared codebase.


## Table of Contents

- [Features](#features)
- [Building](#building)
- [Project layout](#project-layout)
- [Contributing](#contributing)
- [Issues](#issues)
- [License](#license)
- [Credits](#credits)


## Features

- **Chakra and stats**: ten trainable stats (Taijutsu, Ninjutsu, Genjutsu, Kenjutsu, Kinjutsu, Medical,
  Senjutsu, Shurikenjutsu, Speed and Summoning). Most are trained by playing: hitting things, getting hit,
  sprinting, throwing weapons. Scrolls give them a boost. Ninjutsu raises your maximum chakra, Medical your
  health, Taijutsu your damage and Speed your movement speed. You earn Shinobi Points as stats grow.
- **Clans, ranks and villages**: you are assigned a clan on your first join (Fuma, Nara, Shiin, Shirogane,
  Uzumaki, Uchiha, Hyuuga or Chinoike) along with a rank and a hidden village. Each clan changes your
  stats and chakra growth.
- **Doujutsu**: the Sharingan (with its tomoe stages), Byakugan and Ketsuryugan, with their own buffs and
  a screen to equip them and adjust how the eyes look.
- **Chakra Control**: a toggle (see the key bindings) that costs a little chakra over time. While it is on
  you walk on water, run faster and climb walls: walk into a wall and keep holding forward. Look down
  to run back down, hold sneak to slide, and press back to let go. Hanging on a wall slowly drains chakra.
- **Jutsu**: Fire Ball, Earth Wall, Earth Wave, Water Dragon, Water Prison, Shark Bomb and Lightning Chakra
  Mode. They need the matching nature release, cost chakra, and live in a jutsu storage that is bound to you.
- **Swords**: Kubikiribocho, Samehada, Kabutowari, Kiba, Shibuki and Nuibari. Their special abilities need
  Kenjutsu and chakra, and are toggled with the Special Action key.
- **Throwing weapons**: shuriken, kunai, explosive kunai, senbon, poison senbon and the Fuma shuriken.
- **Bosses**: Zabuza Momochi, Jinpachi Munashi and Kisame Hoshigaki walk on water and use throwing weapons.
  They are rare: each player gets one roll every few minutes (see `BossSpawner`).
- **Commands**: `/clan`, `/rank`, `/affiliation`, `/dojutsu`, `/shinobi_stat` and `/shinobi_info`.
  Setting values needs operator permission.


## Building

You need **Java 21**.

```
./gradlew build
```

Each loader writes its jar to its own folder:

| Loader   | Jar                    |
|----------|------------------------|
| NeoForge | `neoforge/build/libs/` |
| Fabric   | `fabric/build/libs/`   |
| Forge    | `forge/build/libs/`    |

Put the jar for your loader in the `mods` folder of a Minecraft 1.21.1 profile. To test in a development
client, run `./gradlew :neoforge:runClient` (or `:fabric:runClient`, `:forge:runClient`).


## Project layout

The project follows the MultiLoader template:

- `common/`: the mod itself. Game logic, items, entities, stats, commands, GUIs and assets.
  It never calls a loader directly. It goes through `Services.PLATFORM` (`IPlatformHelper`).
- `neoforge/`, `fabric/`, `forge/`: per-loader glue only. Registries, event hooks, networking and the
  platform helper that stores player data.

Keep gameplay logic in `common`. The loader modules should just forward events into it, as the stat events
do through `StatProgression` and the client requests through `ServerActions`. That way a fix lands once
for all three loaders.


## Contributing

We welcome contributions to the **Naruto x Boruto** mod! If you have ideas, feature requests, or bug fixes, feel free to contribute.

### How to Contribute from the GitHub Page

1. **Fork the repository**:
   - Click the "Fork" button at the top of this repository page to create a copy of the project under your own GitHub account.
2. **Clone your fork**:
    - In your terminal run:
    ```
    git clone https://github.com/<your-own-username>/Naruto-x-Boruto.git
    ```
    - Navigate into the project directory:
    ```
    cd Naruto-x-Boruto
    ```
3. **Create a new branch**:
    - Create a branch to work on your changes:
    ```
    git checkout -b feature/<your-feature-name>
    ```
4. **Make your changes and commit them**:
    - Make the changes you want to contribute. Afterward, stage and commit your changes:
    ```
    git add .
    git commit -m "<Add your feature description>"
    ```
5. **Push to your fork**:
    - Push your changes to the new branch on your forked repository:
    ```
    git push origin feature/<your-feature-name>
    ```
6. **Create a Pull Request**:
   - Go to the original **Naruto x Boruto** repository on GitHub.
   - You should see an option to **"Compare & Pull Request"**. Click it and write a brief description of your changes.
   - Submit the pull request for review. The maintainers will review your changes, suggest improvements if needed, and merge if approved.

### How to Contribute Directly via Git

1. **Clone the Repository**:
    - Clone the original repository directly (if you have write access):
      ```
      git clone https://github.com/Turgyn123/Naruto-x-Boruto.git
      ```
    - Navigate into the project directory:
      ```
      cd Naruto-x-Boruto
      ```
2. **Create a New Branch**:
    - Create a branch for your changes:
      ```
      git checkout -b feature/<your-feature-name>
      ```
3. **Commit Your Changes**:
    - Make the necessary changes, then stage and commit them:
      ```
      git add .
      git commit -m "<Add description of your feature or fix>"
      ```
4. **Push Your Branch**:
    - Push your changes to the remote repository:
      ```
      git push origin feature/<your-feature-name>
      ```
5. **Create a Pull Request**:
    - Go to the original **Naruto x Boruto** GitHub repository.
    - Open a pull request from the new branch you just pushed.
    - Provide a clear description of your changes, mentioning why they are needed, and submit the PR.

### Contributing Guidelines

- **Keep Your Branch Up-to-Date**: Regularly pull changes from the original repository to keep your fork or branch up-to-date.
- **Code Quality**: Ensure your code follows the project's coding standards.
Add comments where necessary, and make sure the code is clean and readable.
- **Test Your Changes**: Before submitting a PR, thoroughly test your changes to make sure they work as expected.
- **Descriptive Commits**: Write clear, concise, and descriptive commit messages.

By following these steps, you can effectively contribute to the development of the **Naruto x Boruto** mod.
Thank you for your interest in improving the project!


## Issues

If you encounter any bugs, crashes, or have suggestions, please report them in the [Issues](https://github.com/Turgyn123/Naruto-x-Boruto/issues) section of the repository.

### Reporting Bugs

1. Describe the issue in detail.
2. Provide screenshots or logs if possible.
3. Specify the **Minecraft version**, **Forge version**, and **mod version** you are using.


## License

This project is licensed under a custom license.
By downloading, installing or using this software, you agree to the terms outlined in the [LICENSE](LICENSE.txt) file.
Please review these terms carefully.

This license allows personal, non-commercial use only and restricts distribution, commercialization and certain modifications.
For any other usage, please contact Turgyn for permission.


## Credits

- **Mod Developers**: Turgyn123, Tyler; <others>
- **Special Thanks**:
- **Inspired by Naruto & Boruto**: This mod is inspired by the popular anime series Naruto and Boruto, created by Masashi Kishimoto.

---
**Disclaimer**: This is a fan-made mod, not officially affiliated with Naruto, Boruto, or any associated companies.
