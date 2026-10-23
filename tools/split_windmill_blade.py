#!/usr/bin/env python3
"""
Sépare la pale d'éolienne en deux modèles : partie fixe (mât) et rotor (pales, rendu tournant par le renderer).
Usage : split_windmill_blade.py <dossier assets ancientwarfare> <dossier assets ancientwarfarerecompiled>
"""
import importlib.util
import json
import os
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location("obj2json", os.path.join(HERE, "obj_to_json_model.py"))
conv = importlib.util.module_from_spec(spec)
spec.loader.exec_module(conv)


def model(groups, verts, uvs, names, tex):
    elements = [conv.element_for(n, groups[n], verts, uvs, 256, 256, 0.0) for n in names]
    return {"elements": elements, "textures": {"all": tex, "particle": tex}}


def main():
    src, dst = sys.argv[1], sys.argv[2]
    verts, uvs, groups = conv.parse(os.path.join(src, "models/block/automation/windmill_blade.obj"))
    names = [k for k in groups if groups[k]]
    static = [k for k in names if k.startswith("windmillShaft") or k.startswith("bladeShaft")]
    rotor = [k for k in names if k.startswith("blade.") or k.startswith("bladeJoint")]
    tex = "ancientwarfarerecompiled:block/windmill_blade"
    base = os.path.join(dst, "models/block")
    outputs = {
        "windmill_blade_static.json": model(groups, verts, uvs, static, tex),
        "windmill_blade_rotor.json": model(groups, verts, uvs, rotor, tex),
        "windmill_blade.json": model(groups, verts, uvs, names, tex),
    }
    for fname, data in outputs.items():
        with open(os.path.join(base, fname), "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)
    shutil.copy(os.path.join(src, "textures/model/automation/windmill_blade.png"),
                os.path.join(dst, "textures/block/windmill_blade.png"))
    print(f"statique {len(static)} groupes, rotor {len(rotor)} groupes")


if __name__ == "__main__":
    main()
