#include <jni.h>
#include <string>
#include <algorithm>
#include <climits>
#include <android/log.h>
#include <sys/ptrace.h>
#include <unistd.h>
#include <cstdio>
#include <cstring>

#define LOG_TAG "JNI_PREMIUM_DEMO"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Déclaration du scope global externe C
extern "C" {

// =========================================================================
// COUCHE DEFENSIVE PREMIUM (LAB 23) - Inlinée pour masquer les symboles
// =========================================================================

// Contrôle 1 : Détection d'attachement via ptrace
__attribute__((always_inline)) static inline bool isBeingTraced() {
    // Un processus ne peut être tracé (PTRACE_TRACEME) qu'une seule fois.
    // Si un débogueur (comme LLDB) est connecté, l'appel système renvoie -1.
    long result = ptrace(PTRACE_TRACEME, 0, 0, 0);
    if (result == -1) {
        LOGE("[ALERTE SÉCURITÉ] ptrace a échoué ! Un débogueur est actif sur le processus.");
        return true;
    }
    return false;
}

// Contrôle 2 : Analyse de la cartographie mémoire à la recherche de signatures d'outils de hooking
__attribute__((always_inline)) static inline bool containsSuspiciousLibraries() {
    FILE* maps = fopen("/proc/self/maps", "r");
    if (!maps) {
        return false;
    }

    char line[512];
    bool suspect_found = false;

    // Lecture ligne par ligne des modules chargés en mémoire RAM
    while (fgets(line, sizeof(line), maps)) {
        if (strstr(line, "frida") ||
            strstr(line, "xposed") ||
            strstr(line, "libfrida") ||
            strstr(line, "gdbserver") ||
            strstr(line, "magisk")) {
            LOGE("[ALERTE SÉCURITÉ] Signature d'instrumentation repérée dans /proc/self/maps : %s", line);
            suspect_found = true;
            break;
        }
    }
    fclose(maps);
    return suspect_found;
}

// Implémentation de la méthode de vérification globale appelée par Java
jboolean isDebugDetected(JNIEnv* env, jobject thiz) {
    LOGI("-> [C++] Exécution de la ronde des contrôles de sécurité anti-debug.");

    // 🛡️ ON RÉACTIVE LE VRAI CONTRÔLE :
    bool d1 = isBeingTraced();

    return d1 ? JNI_TRUE : JNI_FALSE;
}

// =========================================================================
// FONCTIONS MÉTIERS DU LAB 22 (Conservées intactes)
// =========================================================================

jstring helloFromJNI(JNIEnv* env, jobject thiz) {
    LOGI("-> [C++] helloFromJNI exécuté.");
    return env->NewStringUTF("Hello from C++ via JNI (Signature Dynamique + Anti-Debug) !");
}

jlong factorial(JNIEnv* env, jobject thiz, jint n) {
    if (n < 0) return -1;
    if (n > 20) return -2;
    jlong res = 1;
    for (int i = 1; i <= n; ++i) res *= i;
    return res;
}

jstring reverseString(JNIEnv* env, jobject thiz, jstring javaString) {
    if (javaString == nullptr) return env->NewStringUTF("");
    const char* chars = env->GetStringUTFChars(javaString, nullptr);
    if (chars == nullptr) return nullptr;
    std::string s(chars);
    env->ReleaseStringUTFChars(javaString, chars);
    std::reverse(s.begin(), s.end());
    return env->NewStringUTF(s.c_str());
}

jint sumArray(JNIEnv* env, jobject thiz, jintArray array) {
    if (array == nullptr) return -1;
    jsize len = env->GetArrayLength(array);
    jint* elements = env->GetIntArrayElements(array, nullptr);
    if (elements == nullptr) return -2;
    long long sum = 0;
    for (jsize i = 0; i < len; ++i) sum += elements[i];
    env->ReleaseIntArrayElements(array, elements, JNI_ABORT);
    if (sum > INT_MAX || sum < INT_MIN) return -3;
    return static_cast<jint>(sum);
}

jlong intenseCalculationNative(JNIEnv* env, jobject thiz, jint iterations) {
    jlong count = 0;
    for (int i = 0; i < iterations; ++i) {
        count += (i % 3 == 0) ? (jlong)i * 2 : (jlong)i / 2;
    }
    return count;
}

// =========================================================================
// MISE À JOUR DU SYSTÈME D'ENREGISTREMENT DYNAMIQUE (RegisterNatives)
// =========================================================================

static JNINativeMethod gMethods[] = {
        // Enregistrement de notre nouvelle fonction de sécurité ("()Z" signifie sans paramètre, retour booléen)
        {"isDebugDetected", "()Z", (void*)isDebugDetected},
        {"helloFromJNI", "()Ljava/lang/String;", (void*)helloFromJNI},
        {"factorial", "(I)J", (void*)factorial},
        {"reverseString", "(Ljava/lang/String;)Ljava/lang/String;", (void*)reverseString},
        {"sumArray", "([I)I", (void*)sumArray},
        {"intenseCalculationNative", "(I)J", (void*)intenseCalculationNative}
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass clazz = env->FindClass("com/example/jnidemo/MainActivity");
    if (clazz == nullptr) {
        return JNI_ERR;
    }

    if (env->RegisterNatives(clazz, gMethods, sizeof(gMethods) / sizeof(gMethods[0])) < 0) {
        return JNI_ERR;
    }

    LOGI("[Succès] RegisterNatives : Liaison dynamique Lab 23 mise en place !");
    return JNI_VERSION_1_6;
}

} // extern "C"