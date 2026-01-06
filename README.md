# Monopoly Client-Server Game

> **Course:** Data Structures and Algorithms  
> **Technology:** Java 17 + JavaFX  
> **Architecture:** Client-Server with Socket Communication

## 📖 Project Overview

A multiplayer **Monopoly** simulator for exactly **4 players**, built on a strict **Client-Server architecture**. The core objective is to demonstrate the **manual implementation** and correct application of specific Data Structures to manage game state, logs, and complex logic.

**Key Constraint:** No built-in library collections (e.g., `java.util.LinkedList`, `java.util.HashMap`) for the core data structures. All must be implemented from scratch.

---

## 🏗️ Project Structure

```
monopoly/
├── src/
│   ├── main/
│   │   ├── java/com/monopoly/
│   │   │   ├── datastructures/    # Manual DS implementations (50% of grade)
│   │   │   │   ├── CircularLinkedList.java  # Board traversal
│   │   │   │   ├── Queue.java               # Card decks (FIFO)
│   │   │   │   ├── Stack.java               # Undo/Redo (LIFO)
│   │   │   │   ├── HashTable.java           # O(1) lookups
│   │   │   │   ├── Tree.java                # Player asset hierarchy
│   │   │   │   ├── BST.java                 # Sorted rankings
│   │   │   │   ├── Heap.java                # Top-K queries
│   │   │   │   └── Graph.java               # Financial interactions
│   │   │   │
│   │   │   ├── model/                       # Game entities
│   │   │   │   ├── game/                    # GameState, Board, Dice, Bank
│   │   │   │   ├── player/                  # Player, PlayerAssets
│   │   │   │   ├── tile/                    # All tile types
│   │   │   │   ├── property/                # Property, ColorGroup, Building
│   │   │   │   └── card/                    # Chance & Community Chest cards
│   │   │   │
│   │   │   ├── server/                      # Server-side code
│   │   │   │   ├── Server.java              # Main server
│   │   │   │   ├── ClientHandler.java       # Per-client handler
│   │   │   │   └── GameController.java      # Game logic controller
│   │   │   │
│   │   │   ├── client/                      # Client-side code
│   │   │   │   ├── Client.java              # Main client
│   │   │   │   └── ServerConnection.java    # Network connection
│   │   │   │
│   │   │   ├── logic/                       # Game logic managers
│   │   │   │   ├── GameLogic.java
│   │   │   │   ├── TurnManager.java
│   │   │   │   ├── RentCalculator.java
│   │   │   │   ├── AuctionManager.java
│   │   │   │   ├── TradeManager.java
│   │   │   │   ├── JailManager.java
│   │   │   │   ├── BankruptcyManager.java
│   │   │   │   └── ConstructionManager.java
│   │   │   │
│   │   │   ├── transaction/                 # Atomic transactions
│   │   │   ├── history/                     # Undo/Redo system
│   │   │   ├── analytics/                   # Reports & rankings
│   │   │   │
│   │   │   ├── network/                     # Network protocol
│   │   │   │   ├── protocol/                # Message types
│   │   │   │   └── serialization/           # JSON serialization
│   │   │   │
│   │   │   ├── gui/                         # JavaFX GUI
│   │   │   │   ├── MainApp.java             # Application entry
│   │   │   │   ├── controllers/             # FXML controllers
│   │   │   │   ├── views/                   # Custom view components
│   │   │   │   └── components/              # Reusable UI components
│   │   │   │
│   │   │   └── util/                        # Utilities & constants
│   │   │
│   │   └── resources/
│   │       ├── fxml/                        # FXML layouts
│   │       ├── css/                         # Stylesheets
│   │       ├── images/                      # Game images (TODO)
│   │       └── config/                      # Game configuration
│   │
│   └── test/java/com/monopoly/             # Unit tests
│
├── pom.xml                                  # Maven build file
└── README.md                                # This file
```

---

## 🧱 Data Structures Implementation (50% of Grade)

| Data Structure | Role | Implementation File |
|:---|:---|:---|
| **Circular Linked List** | Board (40 tiles in loop) | `CircularLinkedList.java` |
| **Queue (FIFO)** | Chance & Community Chest decks | `Queue.java` |
| **Stack (LIFO)** | Undo & Redo stacks | `Stack.java` |
| **Hash Table** | O(1) Player/Property lookup | `HashTable.java` |
| **Tree** | Player Asset Hierarchy | `Tree.java` |
| **BST** | Sorted Player Rankings | `BST.java` |
| **Heap** | Top-K Reports | `Heap.java` |
| **Graph** | Financial Interactions | `Graph.java` |

---

## 📡 Architecture

### Server (Single Source of Truth)
- Holds complete `GameState`
- Generates dice rolls
- Enforces all game rules
- Manages data structures
- Handles 4 concurrent TCP connections
- **Atomic transactions** for all money transfers

### Client (Visualization Only)
- Connects via Socket
- Sends commands (`ROLL_DICE`, `BUY_PROPERTY`, etc.)
- Receives `STATE_UPDATE` events
- **Never calculates game logic**
- Only displays what server tells it

### Turn State Machine
```
TURN_START → ROLL → MOVE → DECISION → TURN_END
                              ↓
                        AUCTION / TRADE
```

---

## 🎮 Game Rules (Simplified)

- **4 Players** required
- **GO Bonus:** $200 for passing GO
- **Jail:** Max 2 turns, exit via Doubles/Fine/Card
- **Auction:** Mandatory if player declines to buy
- **Building:** Requires complete Color Group
- **Bankruptcy:** Assets return to Bank, game ends with 1 player

---

## 🛠️ Building & Running

### Prerequisites
- Java 17+
- Maven 3.6+
- JavaFX 17

### Build
```bash
mvn clean compile
```

### Run Server
```bash
mvn exec:java -Dexec.mainClass="com.monopoly.server.ServerMain"
```

### Run Client
```bash
mvn javafx:run
```

---

## 📝 Development Roadmap

1. [ ] Implement all Data Structures
2. [ ] Implement Model classes
3. [ ] Implement Server & Network Protocol
4. [ ] Implement Client connection
5. [ ] Implement Game Logic managers
6. [ ] Implement Transaction system
7. [ ] Implement Undo/Redo
8. [ ] Implement Analytics & Reports
9. [ ] Build JavaFX GUI
10. [ ] Testing & Integration

---

## ⚠️ Important Constraints

1. **No built-in collections** for core data structures
2. **Server is Single Source of Truth** - never update state in client
3. **Atomic transactions** - all-or-nothing for money transfers
4. **Handle disconnections** - server must remain consistent

---

## 📊 Analytics Features

Using Heap & Graph structures:
- Top-K Richest Players
- Top-K Rent Collectors
- Most Financial Interaction pair
- Player Rankings (BST in-order traversal)
