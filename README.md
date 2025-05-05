# FluxForge
A high-fidelity, modular PWR simulator for hands-on reactor physics and plant-operations training.

### Nuclear Data Libraries

FluxForge does **not** store nuclear-data HDF5 files in Git (they are several GB).

1. Download the desired library
    - cd ./data/xs
    - (ENDF/B-VIII.0) wget https://anl.box.com/shared/static/uhbxlrx7hvxqw27psymfbhi7bx7s6u6a.xz
    - (JEFF 3.2) wget https://anl.box.com/shared/static/pb94oxriiipezysu7w4r2qdoufc2epxv.xz
    - tar -xvf ...
2. Ensure `docker/docker-compose.yml` mounts the folder and export  
   `OPENMC_CROSS_SECTIONS=/xs/cross_sections.xml`.
3. Multiple libraries can be swapped by changing the environment variable.
