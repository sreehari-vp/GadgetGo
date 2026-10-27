package com.gadgetgo.catalog.domain;

import java.util.Arrays;
import java.util.Optional;

public enum RepairType {
    SCREEN_REPAIR("screen-repair", "Screen repair"),
    BATTERY_REPLACEMENT("battery-replacement", "Battery replacement"),
    CHARGING_PORT_REPAIR("charging-port-repair", "Charging port repair"),
    CAMERA_REPAIR("camera-repair", "Camera repair"),
    AUDIO_REPAIR("audio-repair", "Speaker and microphone repair"),
    BUTTON_REPAIR("button-repair", "Button repair"),
    KEYBOARD_REPAIR("keyboard-repair", "Keyboard repair"),
    OVERHEATING_INSPECTION("overheating-inspection", "Overheating inspection"),
    TRACKPAD_REPAIR("trackpad-repair", "Trackpad repair"),
    CHARGING_CONTACT_REPAIR("charging-contact-repair", "Charging contact repair"),
    POWER_INSPECTION("power-inspection", "Power inspection"),
    COOLING_INSPECTION("cooling-inspection", "Cooling inspection"),
    STORAGE_REPLACEMENT("storage-replacement", "Storage replacement"),
    CONNECTIVITY_REPAIR("connectivity-repair", "Connectivity repair");

    private final String slug;
    private final String label;

    RepairType(String slug, String label) {
        this.slug = slug;
        this.label = label;
    }

    public String slug() {
        return slug;
    }

    public String label() {
        return label;
    }

    public static Optional<RepairType> fromSlug(String slug) {
        return Arrays.stream(values())
                .filter(type -> type.slug.equals(slug))
                .findFirst();
    }
}
