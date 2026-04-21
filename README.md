# Proiect PAO (Java) - Etapa I: Terraria-Lite Backend Engine

## 🎯 Context și Obiectiv pentru Agentul AI
Ești un asistent AI specializat în programare Java (OOP). Sarcina ta este să generezi codul sursă complet pentru **Etapa I** a unui proiect universitar la materia Programare Avansată pe Obiecte. 

Proiectul este un "backend engine" pentru un joc 2D tip Terraria (axat pe explorare și construcție pașnică). Momentan **NU** se va implementa o interfață grafică (GUI). Totul trebuie testat și afișat în consolă (terminal).

### ⚠️ Așteptările Laborantului (Focus Principal)
Laborantul a specificat următoarele pentru prezentarea proiectului:
> *"mă gândeam la 3-5 minute maxim pentru fiecare să prezinte un overview, o structură, service class pentru business logic."*

Codul generat trebuie să fie extrem de curat, cu o structură OOP clară, iar clasa de `Service` trebuie să conțină logică de business solidă (fizică de bază, manipulare de matrice, colecții), nu doar metode goale.

---

## 📋 Cerințe Tehnice Obligatorii (Conform Fișei de Proiect)
Agentul AI trebuie să asigure respectarea următoarelor reguli:
1. [cite_start]Proiectul să nu prezinte erori de compilare[cite: 4].
2. [cite_start]Clase simple cu atribute `private` / `protected` și metode de acces (getters/setters)[cite: 14].
3. [cite_start]Utilizarea **moștenirii** pentru crearea de clase adiționale și utilizarea lor polimorfică în cadrul colecțiilor[cite: 17].
4. [cite_start]Cel puțin **8 tipuri de obiecte** distincte definite[cite: 11].
5. [cite_start]Cel puțin **2 colecții diferite** (ex: `List`, `Map`, `Set`), dintre care **cel puțin una să fie sortată** (ex: `TreeMap`, `TreeSet`)[cite: 15]. [cite_start]Se vor folosi și array-uri bi-dimensionale pentru harta jocului[cite: 15].
6. [cite_start]Cel puțin **o clasă serviciu** care să expună minim **10 acțiuni/interogări** ale sistemului[cite: 11, 18].
7. [cite_start]O clasă `Main` din care sunt făcute apeluri către servicii pentru a demonstra funcționalitatea[cite: 19].

---

## 🏗️ Arhitectura Sistemului

### 1. Ierarhia Claselor (Minim 8 Obiecte)
Implementează următoarea ierarhie:

* **`Entity`** (Clasă abstractă) - Are `id`, coordonate `x` și `y`.
  * **`Player`** - Moștenește `Entity`. Conține un obiect de tip `Inventory`.
  * **`PassiveMob`** - Moștenește `Entity`. Reprezintă animale pașnice.
  * **`DroppedItem`** - Moștenește `Entity`. Reprezintă un item aruncat pe jos pe hartă.
* **`GameObject`** (Clasă abstractă) - Are `id`, `name`.
  * **`Block`** - Moștenește `GameObject`. Element pentru **Foreground** (solid, are coliziune).
  * **`Wall`** - Moștenește `GameObject`. Element pentru **Background** (nu are coliziune).
  * **`Tool`** - Moștenește `GameObject`. Are o `durability`.
* **`Inventory`** - Gestionează resursele jucătorului.
* **`WorldLayer`** - Gestionează o matrice 2D (array bidimensional) de `GameObject[][]` (lățime x înălțime).
* **`WorldMap`** - Clasa principală a hărții. Conține:
  * Un `WorldLayer` pentru Foreground.
  * Un `WorldLayer` pentru Background.
  * O colecție cu toate `Entity`-urile de pe hartă.

### 2. Colecțiile Necesare
* [cite_start]**Harta (Array-uri bidimensionale):** `GameObject[][]` în interiorul claselor `WorldLayer`[cite: 15].
* [cite_start]**Entitățile (Listă normală):** `List<Entity>` în clasa `WorldMap` pentru a itera prin jucător și mobi[cite: 15].
* [cite_start]**Inventarul (Colecție Sortată):** `TreeMap<String, Integer>` în clasa `Inventory` pentru a stoca denumirea blocului și cantitatea, sortate alfabetic[cite: 15].

---

## ⚙️ Business Logic: Clasa `TerrariaService`
Clasa serviciu trebuie să implementeze următoarele 13 acțiuni/interogări:

1. `generateWorld(int width, int height)`: Inițializează array-urile hărții (Foreground și Background) cu blocuri/pereți de bază (ex: Dirt, Stone).
2. `moveEntity(Entity entity, int deltaX, int deltaY)`: Schimbă coordonatele unei entități, verificând să nu iasă din limitele hărții.
3. `applyGravity(Entity entity)`: Verifică matricea de Foreground la coordonatele `(entity.x, entity.y - 1)`. Dacă este liber (null), entitatea cade (Y scade).
4. `mineForegroundBlock(int x, int y, Player player)`: Elimină `Block`-ul din matricea frontală și adaugă 1 bucată în `TreeMap`-ul inventarului.
5. `placeForegroundBlock(int x, int y, String blockName, Player player)`: Scade cantitatea din inventar și adaugă noul `Block` în matricea frontală, doar dacă locul e gol.
6. `mineBackgroundBlock(int x, int y, Player player)`: Similar cu #4, dar acționează pe matricea de Background. Posibil doar dacă Foreground-ul la aceleași coordonate este gol.
7. `placeBackgroundBlock(int x, int y, String wallName, Player player)`: Plasează un `Wall` în matricea din spate.
8. `dropItem(String itemName, int amount, Player player, WorldMap map)`: Scade din inventar și instanțiază un `DroppedItem` la coordonatele jucătorului, adăugându-l în `List<Entity>`.
9. `pickupItem(Player player, DroppedItem item, WorldMap map)`: Adaugă itemul în inventar și îl șterge din `List<Entity>`.
10. `spawnPassiveMob(String mobName, int x, int y, WorldMap map)`: Creează un `PassiveMob` și îl adaugă în lista de entități.
11. `craftTool(String toolName, Player player)`: Consumă resurse specifice din inventar (ex: 3 Wood) pentru a adăuga un `Tool` în inventar.
12. `getSortedInventory(Player player)`: Interogare care printează frumos în consolă conținutul `TreeMap`-ului (ex: "Dirt: 15, Stone: 5").
13. `printRenderSector(int startX, int startY, int width, int height, WorldMap map)`: O funcție care desenează în consolă o secțiune ASCII a hărții (ex: `#` pentru blocuri solide, `.` pentru background, `P` pentru jucător).

---

## 🚀 Sarcini de Execuție pentru Agentul AI
1. Te rog să generezi codul sursă Java complet pentru clasele menționate mai sus.
2. Structurează codul în pachete logice (ex: `models`, `service`, `main`).
3. Asigură-te că clasa `Main` inițializează jocul, apelează serviciile pentru a construi un scenariu scurt (un jucător se mișcă, sparge 2 blocuri, pune un bloc în background, craftează un obiect și afișează inventarul sortat) și printează rezultatele în consolă.
4. Adaugă comentarii scurte (JavaDoc) la metodele din Service pentru a explica logica, util pentru prezentarea de 3-5 minute a studentului.