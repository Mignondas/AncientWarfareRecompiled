#!/usr/bin/env python3
"""
Génère les modèles des distributeurs et jonctions avec bras selon les connexions.

Pour chaque genre (distributor, junction) et tier (light, medium, heavy) :
- un modèle « corps » (gearBox) ;
- un modèle par direction de bras (downShaft, upShaft, northShaft, ...) ;
- une blockstate multipart : le corps selon FACING, puis chaque bras selon FACING + la connexion.

Usage : split_transport_models.py <dossier assets ancientwarfare> <dossier assets ancientwarfarerecompiled>
"""
import importlib.util
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location("obj2json", os.path.join(HERE, "obj_to_json_model.py"))
conv = importlib.util.module_from_spec(spec)
spec.loader.exec_module(conv)

DIRS = ["north", "south", "east", "west", "up", "down"]
FACINGS = DIRS
ARM_GROUPS = {"downShaft": "down", "upShaft": "up", "northShaft": "north",
              "southShaft": "south", "westShaft": "west", "eastShaft": "east"}
Y_CYCLE = ["north", "east", "south", "west"]
X_CYCLE = ["north", "up", "south", "down"]
ROTATION = {"north": {}, "south": {"y": 180}, "west": {"y": 270}, "east": {"y": 90}, "up": {"x": 270}, "down": {"x": 90}}


def world_of(facing, d):
    """Direction monde d'une direction du modèle canonique (FACING = nord)."""
    if facing in ("up", "down"):
        if d in X_CYCLE:
            steps = 1 if facing == "up" else 3
            return X_CYCLE[(X_CYCLE.index(d) + steps) % 4]
        return d
    steps = Y_CYCLE.index(facing)
    if d in Y_CYCLE:
        return Y_CYCLE[(Y_CYCLE.index(d) + steps) % 4]
    return d


def canon_of(facing, w):
    """Inverse de world_of : direction canonique qui se retrouve sur la face monde w."""
    for d in DIRS:
        if world_of(facing, d) == w:
            return d
    raise ValueError((facing, w))


def build_model(groups, names, verts, uvs, tex, dy):
    elements = []
    for name in names:
        faces = groups[name]
        elements.append(conv.element_for(name, faces, verts, uvs, 64, 64, dy))
    return {"elements": elements, "textures": {"all": tex, "particle": tex}}


def main():
    src, dst = sys.argv[1], sys.argv[2]
    obj = os.path.join(src, "models/block/automation/torque_transport.obj")
    verts, uvs, groups = conv.parse(obj)
    for kind in ("distributor", "junction"):
        for tier in ("light", "medium", "heavy"):
            tex = f"ancientwarfarerecompiled:block/torque_{kind}_{tier}"
            base = os.path.join(dst, "models/block")
            body_names = [k for k in groups if k.startswith("gearBox") and groups[k]]
            body_id = f"ancientwarfarerecompiled:block/torque_{kind}_{tier}_body"
            with open(os.path.join(base, f"torque_{kind}_{tier}_body.json"), "w", encoding="utf-8") as f:
                json.dump(build_model(groups, body_names, verts, uvs, tex, 0.5), f, indent=2)
            parts = [{"when": {"facing": fc}, "apply": {"model": body_id, **ROTATION[fc]}} for fc in FACINGS]
            for arm_group, side in ARM_GROUPS.items():
                names = [k for k in groups if k.startswith(arm_group) and groups[k]]
                arm_id = f"ancientwarfarerecompiled:block/torque_{kind}_{tier}_arm_{side}"
                with open(os.path.join(base, f"torque_{kind}_{tier}_arm_{side}.json"), "w", encoding="utf-8") as f:
                    json.dump(build_model(groups, names, verts, uvs, tex, 0.5), f, indent=2)
            for fc in FACINGS:
                for w in DIRS:
                    c = canon_of(fc, w)
                    arm_id = f"ancientwarfarerecompiled:block/torque_{kind}_{tier}_arm_{c}"
                    parts.append({"when": {"facing": fc, w: "true"}, "apply": {"model": arm_id, **ROTATION[fc]}})
            with open(os.path.join(dst, "blockstates", f"torque_{kind}_{tier}.json"), "w", encoding="utf-8") as f:
                json.dump({"multipart": parts}, f, indent=2)
    print("modèles et blockstates multipart générés")


if __name__ == "__main__":
    main()
