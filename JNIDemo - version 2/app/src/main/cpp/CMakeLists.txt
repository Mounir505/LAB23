# Spécifie la version minimale de CMake requise pour compiler le code natif
cmake_minimum_required(VERSION 3.22.1)

# Donne un nom au projet natif
project("jnidemo")

# Déclare et configure la bibliothèque dynamique (.so) à générer
add_library(
        # Nom final de la bibliothèque. En Java, il sera chargé via "native-lib"
        native-lib

        # Spécifie qu'il s'agit d'une bibliothèque partagée (SHARED) requise par Android (.so)
        SHARED

        # Chemin relatif vers le fichier source C++ à compiler
        native-lib.cpp)

# Recherche la bibliothèque de journalisation d'Android (liblog.so) intégrée au système
# pour nous permettre d'utiliser le fichier <android/log.h>
find_library(
        log-lib
        log)

# Lie (link) impérativement notre bibliothèque 'native-lib' à la bibliothèque de logs d'Android
target_link_libraries(
        native-lib
        ${log-lib})