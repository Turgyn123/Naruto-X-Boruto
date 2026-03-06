# Naruto x Boruto Mod

Welcome to the **Naruto x Boruto** mod for Minecraft!
This mod brings elements from the popular anime series Naruto and Boruto into the world of Minecraft, allowing you to experience ninja life, abilities, and battles like never before.


## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Compatibility](#compatibility)
- [Contributing](#contributing)
- [Issues](#issues)
- [License](#license)


## Introduction

The **Naruto x Boruto** mod is designed to give players a unique gameplay experience by adding characters, weapons, jutsus, and more from the Naruto and Boruto series.
Whether you want to become a ninja, master various jutsus, or battle your way through the world, this mod has something for every fan.


## Features

- **Ninja Weapons**: Use iconic weapons like kunai, shuriken, and more.
- **Jutsus**: Master different jutsus and unleash powerful attacks.
- **Characters**: Meet familiar characters and villains from the Naruto and Boruto series.
- **Ninja Villages**: Explore ninja villages and hidden locations.
- **Custom Items**: Craft and use special items from the series.
- **Quests & Missions**: Take on quests to earn rewards and progress your ninja skills.


## Installation

### Prerequisites

- **Minecraft Version**: 1.21+
- **Java Version**: Ensure you have Java 21 installed.
- **Git**: Make sure you have Git installed to clone the repository. You can download it [here](https://git-scm.com/).

### Steps

1. **Clone the Naruto x Boruto Mod Repository**:
    - Open a terminal and run:
      ```
      git clone https://github.com/Turgyn123/Naruto-x-Boruto.git
      ```
      
    - Navigate into the project directory:
      ```
      cd Naruto-x-Boruto
      ```

2. **Build the Mod**:
    - Use Gradle to build the mod. In the terminal, run:
      ```
      ./gradlew build
      ```
      
    - After the build completes, the `.jar` file will be generated in the `build/libs` directory.

3. **Install Forge**:
    - Make sure you have Minecraft Forge installed. Follow the instructions provided on the Forge website to install it.

4. **Place the Mod File**:
    - Copy the generated `.jar` file from the `build/libs` directory into the `mods` folder located in your Minecraft directory.
    Typically, this folder can be found at:
        - **Windows**: `C:\Users\<Your Username>\AppData\Roaming\.minecraft\mods`
        - **Mac**: `~/Library/Application Support/minecraft/mods`
        - **Linux**: `~/.minecraft/mods`

5. **Launch Minecraft**:
    - Open Minecraft Launcher, choose the Forge profile, and click **Play**.

### Running the Mod Locally for Development

1. **Open Project in Your IDE**:
    - Import the project as a Gradle project in your IDE (e.g., IntelliJ IDEA or Eclipse).

2. **Use the Gradle Task to Run Client**:
    - To run the mod directly for testing, use the Gradle task:
      ```
      ./gradlew runClient
      ```
    - This will start a local Minecraft instance with your mod loaded, allowing you to test and develop without building the `.jar` file each time.

3. **Making Changes and Testing**:
    - As you make changes to the mod, you can re-run the `runClient` Gradle task to see your updates immediately.

TODO: Having the project in WSL and running runClient from there seems to bug out.
Client opens and game starts but mouse controls (at least) seem to not work.


## Usage

- **Access Ninja Skills**: After installing the mod, you can start accessing new ninja tools, jutsus, and items directly in-game.
- **Crafting & Recipes**: Use the in-game crafting table to create special items and weapons.
- **Jutsus**: Learn different jutsus by ...

TODO: More information about gameplay mechanics, how to access specific features, other quick setups


## Compatibility

- **Minecraft Version**: 1.21+
- **Mod Dependencies**: This mod requires Minecraft Either Forge, NeoForge or Fabric for 1.21+. Ensure other mods installed are also compatible with this version of Minecraft.
- **Known Issues**:
    - <Describe any known compatibility issues or conflicts with other mods>

    
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
