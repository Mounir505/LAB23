LAB 23 — JNI + Protection Anti-Debug Native
===========================================

Android NDK • JNI • CMake • Détection défensive native • Protection anti-debug

📌 Présentation du laboratoire
------------------------------

Ce laboratoire constitue une extension directe du précédent TP consacré à JNI. L’objectif est désormais d’ajouter une couche défensive native dans une application Android afin d’illustrer certains mécanismes de protection fréquemment rencontrés dans des applications sensibles.

L’application Android utilisera toujours JNI comme passerelle entre Java et C++, mais cette fois le code natif réalisera également des contrôles liés au débogage et à certains contextes d’instrumentation dynamique.

Dans de nombreuses applications modernes, certaines vérifications sensibles sont volontairement déplacées dans du code natif C/C++, car ce code est généralement plus difficile à analyser, modifier ou contourner qu’une simple logique Java.

Ce laboratoire reste un TP pédagogique défensif. Il doit être réalisé uniquement dans un environnement de test autorisé, sur une application de démonstration personnelle.

Android rappelle officiellement que :

*   le NDK permet d’intégrer du code C/C++ dans Android ;
*   JNI sert d’interface entre Java/Kotlin et le natif ;
*   le code natif doit rester organisé et maintenable ;
*   les transitions Java/native doivent rester limitées et propres.

🎯 Objectifs pédagogiques
-------------------------

À la fin de ce laboratoire, il sera possible de :

*   Intégrer un contrôle anti-debug natif dans Android ;
*   Comprendre le principe de ptrace ;
*   Comprendre l’inspection de /proc/self/maps ;
*   Retourner un booléen natif vers Java ;
*   Adapter le comportement de l’interface Android ;
*   Journaliser des événements natifs dans Logcat ;
*   Structurer un module JNI défensif proprement ;
*   Comprendre les limites d’une approche anti-debug ;
*   Introduire la notion de défense en profondeur mobile.

🧠 Ce que l’application fera
----------------------------

L’application Android reprendra le projet JNIDemo du laboratoire précédent, puis ajoutera une nouvelle méthode native :

    
    public native boolean isDebugDetected();
    

Cette fonction exécutera deux contrôles principaux :

*   un contrôle de type trace/debug attaché ;
*   une inspection de certaines bibliothèques chargées en mémoire.

Si un contexte suspect est détecté, l’application pourra modifier son comportement :

*   affichage d’un état d’alerte ;
*   désactivation de fonctions natives ;
*   blocage logique de certaines actions ;
*   journalisation d’événements dans Logcat.

Dans ce TP, le comportement reste volontairement pédagogique et observable : aucun crash brutal n’est utilisé.

🛠️ Prérequis
-------------

Composant

Description

Android Studio

IDE Android principal

Projet JNIDemo

Projet JNI du TP précédent

NDK

Compilation native Android

CMake

Build système natif

LLDB

Débogueur C/C++ Android

Vérifier dans : **Tools → SDK Manager → SDK Tools** que NDK, CMake et LLDB sont bien installés.

🏗️ Architecture logique du laboratoire
---------------------------------------

    
    MainActivity (Java)
            ↓
    Appel JNI isDebugDetected()
            ↓
    Chargement de libnative-lib.so
            ↓
    Contrôles natifs C++
            ↓
    Analyse ptrace
            ↓
    Inspection /proc/self/maps
            ↓
    Retour booléen vers Java
            ↓
    Adaptation de l’interface Android
    

Cette architecture illustre parfaitement le rôle de JNI : séparer la logique sensible dans une couche native tout en gardant le contrôle fonctionnel dans la couche Java.

🚀 Étape 1 — Réutiliser le projet JNI existant
----------------------------------------------

Ouvrir le projet JNIDemo du laboratoire précédent.

Vérifier la présence des fichiers suivants :

    
    app/src/main/cpp/native-lib.cpp
    app/src/main/cpp/CMakeLists.txt
    

Vérifier également que :

    
    System.loadLibrary("native-lib");
    

est bien présent dans MainActivity.java.

Point de contrôle : le projet doit encore fonctionner normalement avant l’ajout de la logique anti-debug.

⚙️ Étape 2 — Vérifier CMakeLists.txt
------------------------------------

    
    cmake_minimum_required(VERSION 3.22.1)
    
    project("jnidemo")
    
    add_library(
            native-lib
            SHARED
            native-lib.cpp)
    
    find_library(
            log-lib
            log)
    
    target_link_libraries(
            native-lib
            ${log-lib})
    

### Explications détaillées

*   **add\_library()** crée la bibliothèque native ;
*   **SHARED** signifie bibliothèque partagée .so ;
*   **find\_library()** récupère la bibliothèque Android log ;
*   **target\_link\_libraries()** relie les dépendances natives.

🧠 Étape 3 — Comprendre la logique défensive
--------------------------------------------

### 1\. Contrôle ptrace

Le premier contrôle tente de détecter si le processus Android semble déjà supervisé ou attaché à un environnement de debug.

### 2\. Inspection de /proc/self/maps

Sous Linux et Android, le fichier :

    
    /proc/self/maps
    

contient des informations sur les régions mémoire et les bibliothèques chargées dans le processus.

Une application peut analyser ce contenu afin de rechercher certaines signatures textuelles évocatrices.

### 3\. Politique de réaction

Une détection ne doit pas forcément provoquer un crash brutal.

Dans ce laboratoire, la réaction reste pédagogique :

*   journalisation ;
*   statut visuel ;
*   désactivation logique de fonctions ;
*   comportement observable.

💻 Étape 4 — Code natif enrichi
-------------------------------

Le fichier :

    
    native-lib.cpp
    

contiendra désormais :

*   les fonctions JNI du TP précédent ;
*   des contrôles défensifs natifs ;
*   des logs de sécurité ;
*   la méthode isDebugDetected().

### Contrôle ptrace

    
    ptrace(PTRACE_TRACEME, 0, 0, 0);
    

Ce contrôle agit comme un signal simple permettant d’observer certains contextes de débogage.

### Inspection mémoire

    
    /proc/self/maps
    

Le code recherche certaines chaînes évocatrices :

*   frida
*   xposed
*   gdbserver
*   magisk

Ce mécanisme reste une démonstration pédagogique simple et ne constitue pas une détection exhaustive.

📜 Étape 5 — Explication détaillée du code natif
------------------------------------------------

### Bibliothèques importantes

    
    #include <jni.h>
    #include <android/log.h>
    #include <sys/ptrace.h>
    

Bibliothèque

Rôle

jni.h

Fonctions JNI Android

android/log.h

Logs natifs Logcat

sys/ptrace.h

Primitives de traçage système

### Macros de logs

    
    #define LOGI(...)
    #define LOGW(...)
    #define LOGE(...)
    

Elles permettent de produire des messages classés :

*   Information ;
*   Warning ;
*   Error.

### Fonction isBeingTraced()

    
    ptrace(PTRACE_TRACEME, 0, 0, 0);
    

Le but n’est pas d’obtenir une détection parfaite, mais de montrer qu’une logique bas niveau peut être déplacée dans le natif.

### Fonction containsSuspiciousLibraryNames()

    
    fopen("/proc/self/maps", "r");
    

Ouvre la cartographie mémoire du processus Android.

### Recherche de signatures

    
    strstr(line, "frida")
    

Recherche des chaînes textuelles évocatrices dans les bibliothèques chargées.

☕ Étape 6 — Mise à jour de MainActivity.java
--------------------------------------------

### Nouvelle méthode JNI

    
    public native boolean isDebugDetected();
    

### Logique côté interface

Si un état suspect est détecté :

*   message rouge ;
*   désactivation logique ;
*   blocage de fonctions sensibles.

Sinon :

*   statut OK ;
*   appel des fonctions natives ;
*   affichage normal.

Cette approche est plus propre pédagogiquement qu’un crash immédiat ou qu’une fermeture brutale.

🖼️ Étape 7 — Mise à jour du layout XML
---------------------------------------

Le layout Android ajoute maintenant :

*   un TextView d’état sécurité ;
*   des informations JNI ;
*   des résultats de calcul natif.

Le ScrollView reste utilisé afin de garantir une meilleure compatibilité d’affichage.

▶️ Étape 8 — Compilation et exécution
-------------------------------------

### Résultat attendu

    
    Etat securite : OK
    Hello from C++ via JNI !
    Factoriel de 10 = 3628800
    

Si aucun environnement suspect n’est détecté, les fonctions natives restent autorisées.

📜 Étape 9 — Observer Logcat
----------------------------

Ouvrir :

    
    View → Tool Windows → Logcat
    

Filtrer avec :

    
    ANTI_DEBUG
    

### Messages attendus

    
    Aucun trace/debug detecte via ptrace
    Aucune signature suspecte trouvee
    Etat de securite : OK
    

Ou selon le contexte :

    
    Etat suspect : trace/debug detecte
    Signature suspecte trouvee dans maps
    

🧪 Étape 10 — Scénarios de validation
-------------------------------------

Scénario

Objectif

Exécution normale

Observer le comportement standard

Débogage Android Studio

Observer les signaux natifs

Comparaison Logcat

Comparer les états détectés

🔐 Pourquoi utiliser le natif ici
---------------------------------

### 1\. Le code n’est pas uniquement dans le bytecode Java

Une partie sensible de la logique est déplacée dans la bibliothèque native.

### 2\. Contrôles plus proches du système

Le C/C++ interagit plus naturellement avec certaines primitives système Android/Linux.

### 3\. Cloisonnement défensif

Les contrôles sensibles peuvent être regroupés dans un module natif dédié.

✅ Bonnes pratiques importantes
------------------------------

*   Ne jamais dépendre d’un seul contrôle ;
*   Éviter les réactions excessives ;
*   Conserver une API JNI minimale ;
*   Journaliser pendant le développement ;
*   Séparer code métier et logique défensive ;
*   Maintenir un code C++ lisible et structuré.

⚠️ Limites pédagogiques de cette approche
-----------------------------------------

### 1\. Détection imparfaite

Aucun mécanisme simple ne garantit une détection exhaustive.

### 2\. Faux positifs possibles

Certains environnements de développement peuvent déclencher des signaux inattendus.

### 3\. Contre-mesures possibles

Un analyste expérimenté peut parfois contourner ou neutraliser certains contrôles.

### 4\. Complexité croissante

Plus les protections sont nombreuses, plus le code devient difficile à maintenir.

Une bonne stratégie de sécurité mobile repose sur plusieurs couches défensives et non sur une seule protection.

🚀 Variantes d’amélioration
---------------------------

Variante

Description

Variante A

Séparer les contrôles JNI

Variante B

Retourner des codes d’état détaillés

Variante C

Créer un écran d’information sécurité

Variante D

Créer une classe NativeSecurityManager

📚 Résumé pédagogique
---------------------

Dans ce laboratoire, l’application Android :

*   charge une bibliothèque native ;
*   appelle une méthode JNI ;
*   effectue des contrôles défensifs natifs ;
*   analyse certains signaux système ;
*   retourne un booléen vers Java ;
*   adapte dynamiquement l’interface Android.

Ce TP consolide :

*   JNI ;
*   le NDK Android ;
*   CMake ;
*   Logcat natif ;
*   la défense en profondeur mobile.

🏁 Conclusion
-------------

Ce laboratoire représente une excellente transition entre JNI classique et logique défensive native Android.

Il montre comment Android peut s’appuyer sur :

*   JNI ;
*   le NDK ;
*   CMake ;
*   les bibliothèques natives .so ;
*   les primitives Linux/Android.

Le résultat obtenu n’est pas une “protection parfaite”, mais une démonstration réaliste de durcissement progressif d’une application Android moderne.

Ce laboratoire constitue une excellente base pour aller vers : anti-tampering, détection root, sécurité mobile avancée, protection applicative Android, ou instrumentation défensive native.

LAB 23 — JNI + Protection Anti-Debug Native | Android NDK • JNI • CMake • Sécurité Mobile Android
