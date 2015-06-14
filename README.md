# SpiralInductorFootprintGenerator
A java utility for generating helical or square inductor footprints in either gEDA footprint or Kicad legacy module format.

The user can quickly and easily generate a square or helical PCB inductor of specified dimensions using command line options.

Users can then add suitable pins or tracks with a footprint editor or text editor to effect connections to adjacent components.

Users will typically have a required inductance in mind, for which a certain number of turns, line spacing, track width, copper thickness and PCB material will provide the necessary inductance.


Building:

download SpiralInductorFootprintGenerator.java to a working directory

compile it using a java compiler

	javac SpiralInductorFootprintGenerator.java

the resulting java class file can be used with a runtime java virtual machine implementation


Usage:

	java SpiralInductorFootprintGenerator -option value

		-k	export a kicad module, default is geda .fp file

		-s	export a square inductor, default is circular

		-i long	 inner diameter of coil in nanometres

		-o long	 outer diameter of coil in nanometres

		-w long	 track width in nanometres

		-n long	 number of turns

		-l long	 length of segment used to approximate circular arc in nanometres

		-h	 prints this

Example usage:

	java SpiralInductorFootprintGenerator -n 40 -i 15000 -o 50000 -w 250 -l 3000

	generates a 40 turn helical inductor footprint,
	of 15mm inside diameter, 50 mm outside diameter,
	with 0.25mm track width and 3mm segment length.


SpiralInductorFootprintGenerator.java v1.0
Copyright (C) 2015 Erich S. Heinzle, a1039181@gmail.com

    see LICENSE-gpl-v2.txt for software license
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

