# Clean Energy Optimizer

Aplikacja backendowa do optymalizacji ładowania na podstawie miksu energetycznego.  

---

## Technologie
- Java 21
- Spring Boot
- Testy: JUnit 5, Mockito

---

## Funkcjonalności

- **3-dniowy miks energetyczny** 
`/energy-mix`
    - Zwraca procent czystej energii dla kolejnych 3 dni
    - Wylicza średni udział poszczególnych źródeł energii
- **Obliczanie optymalnego okna ładowania** `/energy-mix/optimal-window`
    - Parametr: liczba godzin ładowania
    - Zwraca datę rozpoczęcia i zakończenia optymalnego przedziału ładowania, oraz procent czystej energii w tym oknie.
