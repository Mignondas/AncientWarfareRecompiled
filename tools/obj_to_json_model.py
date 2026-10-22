#!/usr/bin/env python3
"""
Convertit un OBJ de bloc d'Ancient Warfare 2 en modèle JSON de bloc (éléments cuboïdes, UV par face).

Chaque groupe de l'OBJ doit être une boîte alignée sur les axes (6 faces, quads), ce qui est le cas
pour les arbres de couple et la manivelle. Les UV sont normalisées sur 0..16 (pas de texture_size) : Minecraft étale alors toute la texture sur 0..16.

Usage :
  obj_to_json_model.py <fichier.obj> <largeur_atlas> <hauteur_atlas> (non utilisés, conservés pour compatibilité) <decalage_y> <id_texture> <sortie.json>

<decalage_y> est ajouté à Y (en blocs) : 0.5 pour les arbres (l'OBJ est centré en Y=0), 0 pour la manivelle.
"""
import json
import math
import sys
from collections import defaultdict

EPS = 1e-6
DIRECTIONS = {
    (0, 0, -1): "north", (0, 0, 1): "south",
    (-1, 0, 0): "west", (1, 0, 0): "east",
    (0, 1, 0): "up", (0, -1, 0): "down",
}


def parse(path):
    verts, uvs = [], []
    groups = defaultdict(list)
    current = "default"
    with open(path, encoding="utf-8") as f:
        for line in f:
            parts = line.split()
            if not parts:
                continue
            if parts[0] == "v":
                verts.append(tuple(float(x) for x in parts[1:4]))
            elif parts[0] == "vt":
                uvs.append(tuple(float(x) for x in parts[1:3]))
            elif parts[0] in ("g", "o") and len(parts) > 1:
                current = " ".join(parts[1:])
            elif parts[0] == "f":
                face = []
                for token in parts[1:]:
                    items = token.split("/")
                    vi = int(items[0]) - 1
                    ti = int(items[1]) - 1 if len(items) > 1 and items[1] else None
                    face.append((vi, ti))
                groups[current].append(face)
    return verts, uvs, groups


def _rotate(point, axis, theta, pivot):
    """Tourne un point autour d'un axe (x, y ou z) passant par pivot, angle en degrés."""
    c, s = math.cos(theta), math.sin(theta)
    x, y, z = (point[i] - pivot[i] for i in range(3))
    if axis == 0:
        y, z = y * c - z * s, y * s + z * c
    elif axis == 1:
        z, x = z * c - x * s, z * s + x * c
    else:
        x, y = x * c - y * s, x * s + y * c
    return (x + pivot[0], y + pivot[1], z + pivot[2])


def _is_aligned(faces, verts, pivot, axis, theta):
    """Vrai si, après rotation inverse de theta, chaque quad est plan et aligné sur un axe."""
    for face in faces:
        pts = [_rotate(verts[vi], axis, -theta, pivot) for vi, _ in face]
        if not any(max(p[a] for p in pts) - min(p[a] for p in pts) < 1e-4 for a in range(3)):
            return False
    return True


def find_rotation(faces, verts):
    """Retourne (axe, angle_degrés, pivot_obj) pour que le groupe devienne une boîte alignée."""
    points = {verts[vi] for face in faces for vi, _ in face}
    mins = [min(p[i] for p in points) for i in range(3)]
    maxs = [max(p[i] for p in points) for i in range(3)]
    center = [(mins[i] + maxs[i]) / 2 for i in range(3)]
    for axis in range(3):
        pivot = list(center)
        candidates = {0.0}
        for face in faces:
            for k in range(len(face)):
                a = verts[face[k][0]]
                b = verts[face[(k + 1) % len(face)][0]]
                u, w = [i for i in range(3) if i != axis]
                angle = math.degrees(math.atan2(b[w] - a[w], b[u] - a[u]))
                angle = (angle + 45) % 90 - 45
                candidates.add(round(angle, 3))
        for theta_deg in sorted(candidates, key=lambda t: abs(t)):
            if _is_aligned(faces, verts, pivot, axis, math.radians(theta_deg)):
                return axis, theta_deg, pivot
    return None


ALLOWED_ANGLES = (0.0, 22.5, -22.5, 45.0, -45.0)


def approx_element(name, faces, verts, uvs, width, height, dy):
    """Pour un groupe tourné d'un angle non autorisé par Minecraft (seuls 0, ±22.5 et ±45 le sont) :
    boîte alignée sur l'emprise réelle du groupe. La forme est approchée, pas exacte."""
    points = [verts[vi] for face in faces for vi, _ in face]
    mins = [min(p[i] for p in points) for i in range(3)]
    maxs = [max(p[i] for p in points) for i in range(3)]
    face_json = {}
    for face in faces:
        pts = [verts[vi] for vi, _ in face]
        # Normale du quad (produit vectoriel), puis axe dominant pour choisir la face de la boîte.
        a, b, c = pts[0], pts[1], pts[2]
        n = [(b[1]-a[1])*(c[2]-a[2]) - (b[2]-a[2])*(c[1]-a[1]),
             (b[2]-a[2])*(c[0]-a[0]) - (b[0]-a[0])*(c[2]-a[2]),
             (b[0]-a[0])*(c[1]-a[1]) - (b[1]-a[1])*(c[0]-a[0])]
        ax = max(range(3), key=lambda i: abs(n[i]))
        step = [0, 0, 0]
        step[ax] = 1 if n[ax] > 0 else -1
        key = DIRECTIONS[tuple(step)]
        if all(ti is not None for _, ti in face):
            us = [uvs[ti][0] * 16 for _, ti in face]
            vs = [(1.0 - uvs[ti][1]) * 16 for _, ti in face]
            box = [round(min(us), 4), round(min(vs), 4), round(max(us), 4), round(max(vs), 4)]
            if key in face_json and "uv" in face_json[key]:
                o = face_json[key]["uv"]
                box = [min(o[0], box[0]), min(o[1], box[1]), max(o[2], box[2]), max(o[3], box[3])]
            face_json[key] = {"texture": "#all", "uv": box}
        else:
            face_json.setdefault(key, {"texture": "#all"})
    return {
        "from": [round(mins[0] * 16, 4), round((mins[1] + dy) * 16, 4), round(mins[2] * 16, 4)],
        "to": [round(maxs[0] * 16, 4), round((maxs[1] + dy) * 16, 4), round(maxs[2] * 16, 4)],
        "faces": face_json,
    }


def element_for(name, faces, verts, uvs, width, height, dy):
    found = find_rotation(faces, verts)
    if found is None:
        return approx_element(name, faces, verts, uvs, width, height, dy)
    axis, theta_deg, pivot = found
    if round(theta_deg, 1) not in ALLOWED_ANGLES:
        return approx_element(name, faces, verts, uvs, width, height, dy)
    theta = math.radians(theta_deg)
    # Points dans le repère non tourné : l'élément JSON est une boîte alignée.
    unrot = {vi: _rotate(verts[vi], axis, -theta, pivot) for face in faces for vi, _ in face}
    points = list(unrot.values())
    mins = [min(p[i] for p in points) for i in range(3)]
    maxs = [max(p[i] for p in points) for i in range(3)]

    face_json = {}
    for face in faces:
        pos = [unrot[vi] for vi, _ in face]
        direction = None
        for ax in range(3):
            values = [p[ax] for p in pos]
            if max(values) - min(values) > EPS:
                continue
            step = [0, 0, 0]
            step[ax] = -1 if abs(values[0] - mins[ax]) < 1e-4 else 1
            direction = tuple(step)
            break
        if direction is None or direction not in DIRECTIONS:
            raise ValueError(f"{name}: face non alignée après rotation")

        key = DIRECTIONS[direction]
        entry = {"texture": "#all"}
        if all(ti is not None for _, ti in face):
            us = [uvs[ti][0] * 16 for _, ti in face]
            vs = [(1.0 - uvs[ti][1]) * 16 for _, ti in face]
            box = [min(us), min(vs), max(us), max(vs)]
            entry["uv"] = [round(v, 4) for v in box]
        face_json[key] = entry

    element = {
        "from": [round(mins[0] * 16, 4), round((mins[1] + dy) * 16, 4), round(mins[2] * 16, 4)],
        "to": [round(maxs[0] * 16, 4), round((maxs[1] + dy) * 16, 4), round(maxs[2] * 16, 4)],
        "faces": face_json,
    }
    if abs(theta_deg) > 1e-6:
        if abs(theta_deg) > 45 + 1e-6:
            raise ValueError(f"{name}: angle {theta_deg} hors limite Minecraft")
        origin = [pivot[i] * 16 for i in range(3)]
        origin[1] += dy * 16
        element["rotation"] = {
            "origin": [round(v, 4) for v in origin],
            "axis": "xyz"[axis],
            "angle": round(theta_deg, 3),
        }
    return element


def main():
    if len(sys.argv) != 7:
        sys.exit(__doc__)
    obj, width, height, dy, texture_id, out = sys.argv[1], int(sys.argv[2]), int(sys.argv[3]), float(sys.argv[4]), sys.argv[5], sys.argv[6]
    verts, uvs, groups = parse(obj)
    elements = [element_for(name, faces, verts, uvs, width, height, dy) for name, faces in groups.items() if faces]
    model = {
        "elements": elements,
        "textures": {"all": texture_id, "particle": texture_id},
    }
    with open(out, "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)
    print(f"{out}: {len(elements)} éléments")


if __name__ == "__main__":
    main()
