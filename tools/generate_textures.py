#!/usr/bin/env python3
"""Генерация 16x16 текстур посохов и снарядов. Запуск: python3 tools/generate_textures.py (нужен Pillow)."""
import math
import os
from PIL import Image

OUT = os.path.join(os.path.dirname(__file__), "..", "src/main/resources/assets/arcanestaves/textures/item")
os.makedirs(OUT, exist_ok=True)


def new():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def px(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), c)


def shaft(img, n):
    """Древко по диагонали снизу-слева вверх-вправо, n+1 клеток."""
    mid, dark, light = (112, 74, 40, 255), (66, 42, 22, 255), (160, 112, 64, 255)
    for i in range(n + 1):
        x, y = 1 + i, 14 - i
        px(img, x, y, mid)
        px(img, x + 1, y, dark)
        if i % 3 == 0:
            px(img, x, y, light)
    # Наконечник-рукоять
    px(img, 0, 15, dark)


def fire_staff():
    img = new()
    shaft(img, 9)
    cx, cy = 11, 4
    for dx in range(-3, 4):
        for dy in range(-3, 4):
            d = abs(dx) + abs(dy)
            if d > 3:
                continue
            if d == 3:
                c = (130, 22, 10, 255)
            elif d == 2:
                c = (232, 76, 20, 255)
            elif d == 1:
                c = (255, 150, 30, 255)
            else:
                c = (255, 225, 90, 255)
            px(img, cx + dx, cy + dy, c)
    px(img, cx - 1, cy - 1, (255, 240, 170, 255))
    return img


def swap_staff():
    img = new()
    shaft(img, 8)
    x0, y0 = 8, 0  # рама 7x7: x 8..14, y 0..6
    for x in range(x0, x0 + 7):
        for y in range(y0, y0 + 7):
            edge = x in (x0, x0 + 6) or y in (y0, y0 + 6)
            px(img, x, y, (92, 96, 112, 255) if edge else (24, 30, 40, 255))
    for cxy in [(x0, y0), (x0 + 6, y0), (x0, y0 + 6), (x0 + 6, y0 + 6)]:
        px(img, *cxy, (170, 175, 190, 255))
    cx, cy = x0 + 3, y0 + 3
    for dx in range(-2, 3):
        for dy in range(-2, 3):
            d = abs(dx) + abs(dy)
            if d > 2:
                continue
            c = (24, 140, 150, 255) if d == 2 else (64, 226, 214, 255) if d == 1 else (210, 255, 255, 255)
            px(img, cx + dx, cy + dy, c)
    return img


def ring(img, cx, cy, rx, ry, front, color):
    for i in range(0, 360, 6):
        t = math.radians(i)
        x = cx + round(rx * math.cos(t))
        y = cy + round(ry * math.sin(t))
        if (math.sin(t) > 0) == front:
            px(img, x, y, color)


def sphere(img, cx, cy, r):
    for x in range(cx - r - 1, cx + r + 2):
        for y in range(cy - r - 1, cy + r + 2):
            d = math.hypot(x - cx, y - cy)
            if d <= r + 0.3:
                if d > r - 0.9:
                    c = (112, 44, 190, 255)
                elif d > r - 1.8:
                    c = (46, 14, 78, 255)
                else:
                    c = (10, 4, 18, 255)
                px(img, x, y, c)
    px(img, cx - 1, cy - 1, (176, 112, 244, 255))


def singularity_staff():
    img = new()
    shaft(img, 7)
    cx, cy = 10, 6
    white = (240, 240, 255, 255)
    ring(img, cx, cy, 4.4, 1.8, False, white)  # задняя часть кольца
    sphere(img, cx, cy, 2)
    ring(img, cx, cy, 4.4, 1.8, True, white)   # передняя часть кольца
    return img


def swap_bolt():
    img = new()
    for x in range(16):
        for y in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 2.2:
                px(img, x, y, (220, 255, 255, 255))
            elif d <= 3.6:
                px(img, x, y, (64, 226, 214, 255))
            elif d <= 5.2:
                px(img, x, y, (24, 140, 150, 180))
    return img


def singularity_orb():
    img = new()
    white = (240, 240, 255, 255)
    ring(img, 7, 7, 6.0, 2.2, False, white)
    sphere(img, 7, 7, 4)
    ring(img, 7, 7, 6.0, 2.2, True, white)
    return img


for name, fn in [("fire_staff", fire_staff), ("swap_staff", swap_staff), ("singularity_staff", singularity_staff),
                 ("swap_bolt", swap_bolt), ("singularity_orb", singularity_orb)]:
    fn().save(os.path.join(OUT, name + ".png"))
    print("saved", name)
