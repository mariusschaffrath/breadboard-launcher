#pragma once

// Compatibility header for Android API < 24 where getifaddrs/freeifaddrs
// are not available in Bionic libc.
#include_next <ifaddrs.h>
#include <stddef.h>

#if defined(__ANDROID__) && __ANDROID_API__ < 24
#ifdef __cplusplus
extern "C" {
#endif

__attribute__((weak)) inline int getifaddrs(struct ifaddrs** ifap) {
    if (ifap != NULL) {
        *ifap = NULL;
    }
    return -1;
}

__attribute__((weak)) inline void freeifaddrs(struct ifaddrs* ifa) {
    (void)ifa;
}

#ifdef __cplusplus
}
#endif
#endif
