# 🎩 Monopoly Online

A fully-featured multiplayer implementation of the classic Monopoly board game, built with Java and JavaFX.

![Java](https://img.shields.io/badge/Java-17-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-17-blue)
![License](https://img.shields.io/badge/License-MIT-green)

## ✨ Features

- 🎮 **Multiplayer Support** - Play with 2-6 players over network
- 🖥️ **Beautiful Dark Theme UI** - Modern, eye-friendly interface with animations
- 🎲 **Animated Dice Rolling** - Smooth dice animations with doubles detection
- 🏠 **Full Property Management** - Buy, sell, mortgage, build houses & hotels
- 💱 **Trading System** - Propose and negotiate trades with other players
- 🔨 **Auction System** - Bid on properties with real-time timer
- 🃏 **Chance & Community Chest** - All classic cards implemented
- 🏛️ **Jail System** - Pay fine, use card, or roll doubles to escape
- 📊 **Game Statistics** - Track your performance throughout the game

## 🎯 Game Rules

### Objective
Be the last player remaining with money! Bankrupt all other players by collecting rent from your properties.

### Basic Rules

1. **Starting the Game**
   - Each player starts with $1500
   - Players take turns rolling dice and moving clockwise around the board

2. **Buying Properties**
   - Land on an unowned property → Buy it or put it up for auction
   - Own all properties of a color → You have a monopoly!

3. **Building**
   - Must own all properties in a color group to build
   - Build houses evenly across your monopoly
   - 4 houses → Can upgrade to a hotel

4. **Rent**
   - Other players landing on your property must pay rent
   - Rent increases with houses/hotels
   - Mortgaged properties don't collect rent

5. **Special Spaces**
   - **GO** - Collect $200 when passing
   - **Jail** - Just visiting, or sent to jail
   - **Free Parking** - Safe space, no action
   - **Income/Luxury Tax** - Pay the bank

6. **Getting Out of Jail**
   - Pay $50 fine
   - Use "Get Out of Jail Free" card
   - Roll doubles (3 attempts max)

7. **Bankruptcy**
   - Can't pay a debt? You're bankrupt!
   - Your properties go to your creditor or back to the bank

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 17 |
| GUI Framework | JavaFX 17 |
| Build Tool | Maven |
| Networking | Java Sockets (TCP) |
| Serialization | Custom JSON Protocol |

### Custom Data Structures

This project implements custom data structures from scratch:

- **ArrayList** - Dynamic array implementation
- **LinkedList** - Doubly linked list
- **Stack** - LIFO data structure
- **Queue** - FIFO data structure
- **HashTable** - Key-value storage with collision handling
- **BST** - Binary Search Tree
- **Heap** - Priority queue implementation
- **Graph** - For analytics and property relationships

## 📦 Installation

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Clone & Build

```bash
git clone https://github.com/Kourosh37/monopoly.git
cd monopoly
mvn clean install
```

## 🚀 How to Run

### Option 1: Host a Game

**Step 1:** Start the server
```bash
mvn exec:java -Dexec.mainClass="com.monopoly.server.ServerMain"
```

**Step 2:** Launch the client
```bash
mvn javafx:run
```

**Step 3:** In the connection screen:
- Enter your name
- Server: `localhost`
- Port: `12345`
- Click "Connect to Server"

### Option 2: Join an Existing Game

```bash
mvn javafx:run
```

Then enter the host's IP address and port.

### Playing with Friends on LAN

1. Host starts the server on their machine
2. Host shares their local IP (e.g., `192.168.1.100`)
3. Friends connect using that IP and port `12345`

## 🎮 Controls

| Action | How |
|--------|-----|
| Roll Dice | Click "🎲 ROLL DICE" button |
| Buy Property | Click "Buy" when prompted |
| Build House | Click "Build House" (must own monopoly) |
| Trade | Click "💱 Propose Trade" |
| End Turn | Click "✓ END TURN" |
| Mortgage | Click "Mortgage" on property panel |

## 📁 Project Structure

```
monopoly/
├── src/main/java/com/monopoly/
│   ├── client/          # Client networking
│   ├── server/          # Server & game rooms
│   ├── model/           # Game entities (Player, Property, etc.)
│   ├── logic/           # Game rules & logic
│   ├── gui/             # JavaFX controllers & UI
│   ├── datastructures/  # Custom data structures
│   ├── network/         # Protocol & serialization
│   └── analytics/       # Game statistics
├── src/main/resources/
│   ├── fxml/            # UI layouts
│   ├── css/             # Dark theme styles
│   └── images/          # Game assets
└── src/test/            # Unit tests
```

## 🎨 Screenshots

### Connection Screen
- Modern dark theme login
- Token selection with emojis
- Host or join game options

### Game Board
- Classic Monopoly layout
- Animated player tokens
- Real-time game log

### Trade Dialog
- Intuitive property selection
- Money negotiation
- Accept/decline interface

## ⚙️ Configuration

Server settings can be modified in `ServerMain.java`:

```java
int port = 12345;        // Server port
int minPlayers = 2;      // Minimum players to start
int maxPlayers = 6;      // Maximum players per room
```

## 🧪 Running Tests

```bash
mvn test
```

## 📝 Notes

- The game uses TCP sockets for reliable communication
- All game state is managed server-side to prevent cheating
- The UI updates in real-time using JavaFX's Platform.runLater()
- Custom data structures are used instead of Java Collections

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Original Monopoly game by Hasbro
- JavaFX community for UI inspiration
- All contributors and testers

---

**Enjoy the game! 🎲🏠💰**
