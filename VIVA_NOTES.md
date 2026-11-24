# Viva notes - where each syllabus topic is used

| Syllabus topic | Where |
|---|---|
| Classes, objects, constructors, this/static | model/*, Alert has 2 overloaded constructors, `EmergencyFactory` has static methods |
| Inheritance + abstraction + polymorphism | `Emergency` (abstract) -> Medical/Fire/Accident/Crime/WomenSafety/NaturalDisaster/Other. Each overrides getSeverity(), getAdvice() and getContactInstruction(); Emergency.buildMessage() builds the alert message |
| Interfaces | Identifiable, Jsonable, Notifiable (Contact), Comparable<Contact>, Repository<T> |
| Searching (DSA) | ContactService.searchContacts: linear search over the contact list, `toLowerCase().contains(word)` on name, relation, phone |
| Date and time | Alert stores `createdAt` (long milliseconds); `Date` + `SimpleDateFormat` give the separate date and clock strings |
| Strings, StringBuilder | Validator.toTitleCase, Json.quote, Alert.toJson, CsvUtil (replace) |
| Wrapper classes | Integer.parseInt (Validator, repositories), Long.parseLong, Integer in stats map (autoboxing) |
| Checked/unchecked exceptions, custom, throws, multi-catch, finally | exception/*, CsvRepository.load() (multi-catch + finally), BaseHandler.handle() |
| Collections | ArrayList, LinkedList (SharedQueue), HashMap (sessions), LinkedHashMap (repositories), HashSet (duplicate phone), TreeMap (stats), PriorityQueue (SOS order) |
| Iterator | AlertService.getStats |
| Comparable / Comparator / Collections.sort | Contact.compareTo + ContactService.getContactsSorted; newestFirst Comparator in AlertService.getHistory |
| Threads, sleep, daemon | DispatcherThread (extends Thread) |
| synchronized, wait/notifyAll, producer-consumer | SharedQueue<T> (put = producer, take = consumer), synchronized methods in repositories and Alert |
| Lambda + Stream API | AlertService.getHistory: filter by category, filter by keyword (contains), sorted, limit, collect |
| Generics | Repository<T extends Identifiable>, CsvRepository<T>, SharedQueue<T>, Json.array(List<? extends Jsonable>) |
| File handling | CsvRepository (BufferedReader/FileReader, BufferedWriter/FileWriter), StaticFileHandler (FileInputStream) |
| JDBC | Stage 2 |

## Not in syllabus (be ready to say this honestly)
`com.sun.net.httpserver.HttpServer` is only used inside the `server` package so the browser can talk to Java.
Everything else is plain syllabus Java. Passwords are plain text because this is a simulation
(a real app would hash them).

## Likely questions
1. Why is Emergency abstract? Because "an emergency" alone has no severity or advice; each type must define its own.
2. What is polymorphism here? `emergency.getAdvice()` runs a different method for Fire, Medical, etc.
3. Why PriorityQueue in AlertService? It always returns the contact with the smallest priority number first (uses Contact.compareTo).
4. Why Comparable on Contact but Comparator for alerts? Contacts have one natural order (priority). Alerts can be sorted in many ways, so we pass a Comparator.
5. What does synchronized do? Only one thread at a time can run that method on the same object. The HTTP thread and the dispatcher thread both use Alert and the repositories.
6. Why while(...) wait() and not if? The thread must re-check the condition after waking up.
7. Why notifyAll()? To wake threads waiting on the same queue (producer or consumer).
8. Why is the dispatcher a daemon thread? So it does not keep the program alive when the server stops.
9. Why is AppException checked? We want every caller to handle failures (wrong input, file error) on purpose.
10. What is finally used for? To close the file even if an exception happens.
11. Why a generic CsvRepository<T>? One class does file reading/writing for User, Contact and Alert; only toCsv/fromCsv differ.
12. How will JDBC fit in Stage 2? New repository classes implement the same interfaces; nothing else changes.
13. Why encode text before writing CSV? A comma or new line inside text would break the line format.
14. How does the keyword search work? A stream filter lowercases location and details and checks contains(word); the browser sends it as ?q=word.
15. What does "I am safe" do? PUT /api/alerts/{id}/resolve -> AlertService.resolveAlert checks the alert belongs to the user, calls markResolved() and saves it to the CSV.
16. How does contact search work? A linear search: loop through every contact and keep it if the name, relation or phone contains the keyword (both lowered to the same case). It is O(n), fine for max 10 contacts. A binary search would need the list sorted by one key and only helps for exact match.
17. Where is the alert message created? Emergency.buildMessage(): it puts the user name, place, details and a category-specific instruction together with StringBuilder. It is stored in Alert and in the CSV.
18. How is date/time stored? As a long (milliseconds since 1970). Display uses Date and SimpleDateFormat, so we can show date and time separately.
19. Difference between List and Set here? List keeps order and allows duplicates; HashSet is used only to check "does this phone exist" quickly.
