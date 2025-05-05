import openmc
import sys

sp = openmc.StatePoint(sys.argv[1] if len(sys.argv) > 1 else "statepoint.100.h5")
t  = sp.get_tally(name="flux_fission")
flux   = t.get_values(scores=["flux"]).flatten()
fisson = t.get_values(scores=["fission"]).flatten()
for idx,(f,fi) in enumerate(zip(flux,fisson), start=1):
    print(f"Cell {idx}: flux={f:.5e}, fission rate={fi:.5e}")
