// SpiralInductorFootprintGenerator.java v1.0
// Copyright (C) 2015 Erich S. Heinzle, a1039181@gmail.com

//    see LICENSE-gpl-v2.txt for software license
//    see README.txt
//    
//    This program is free software; you can redistribute it and/or
//    modify it under the terms of the GNU General Public License
//    as published by the Free Software Foundation; either version 2
//    of the License, or (at your option) any later version.
//    
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//    
//    You should have received a copy of the GNU General Public License
//    along with this program; if not, write to the Free Software
//    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
//    
//    SpiralInductorFootprintGenerator.java Copyright (C) 2015 Erich S. Heinzle a1039181@gmail.com



import java.lang.Math;
import java.io.*;

public class SpiralInductorFootprintGenerator
{

	public static void main(String[] args) throws IOException
	{


		// some default values for a spiral inductor vaguely OK for Qi applications

		long innerDiameter = 27100; // nm
                long outerDiameter = 32000; // nm
                long segmentLength = 1000; // 1mm length
                int turnsTotal = 9;
                long trackWidth = 220; // nm
                        // (((outerDiameter-innerDiameter)/2-(turnsTotal-1)*trackGap))/turnsTotal;

                boolean theOneTrueEDAsuiteGEDA = true;
                	// obviously, !theOneTrueEDAsuiteGEDA = kicad :-)

		boolean squareCoil = false;

		// we now parse arguments parsed via the command line

		if (args.length == 0)
		{
			printUsage();
			System.exit(0);
		}

		for (int counter = 0; counter < args.length; counter++)
		{
			if (args[counter].startsWith("-n"))
			{
				turnsTotal = Integer.parseInt(args[counter+1]);
				counter++;
			}
                        else if (args[counter].startsWith("-i"))
                        {
                                innerDiameter = Long.parseLong(args[counter+1]);
                                counter++;
                        }
                        else if (args[counter].startsWith("-o"))
                        {
                                outerDiameter = Long.parseLong(args[counter+1]);
                                counter++;
                        }
                        else if (args[counter].startsWith("-l"))
                        {
                                segmentLength = Long.parseLong(args[counter+1]);
                                counter++;
                        }
                        else if (args[counter].startsWith("-w"))
                        {
                                trackWidth = Long.parseLong(args[counter+1]);
                                counter++;
                        }
                        else if (args[counter].startsWith("-k"))
                        {
                                theOneTrueEDAsuiteGEDA = false;
                        }
			else if (args[counter].startsWith("-s"))
			{
				squareCoil = true;
			}
                        else 
                        {
                                printUsage();
				System.exit(0);
                        }
			
		}

		// we now sort out appropriate angular step sizes for the loops and
		// the loop spacings based on the inner and outer dimensions given

		double theta = 0;
		double nextTheta = 0;
		double startRadius = innerDiameter/2.0;
		double nextRadius = startRadius;

		// we figure out the circumference, well, at least a reasonable
		// approximation of a real number using the set of long integers
		long circumference = (long)Math.PI * outerDiameter;

		// we base segments per loop on the outermost loop circumference
		double segmentsPerLoop = Math.PI*outerDiameter/segmentLength;
		// we figure out a step size in radians to step around the spiral
                // which is 2pi radians divided by number of segments
		double deltaTheta = (2.0 * Math.PI)/segmentsPerLoop;

//		System.out.println("deltaTheta :" + deltaTheta);

		// we now define some flags
		boolean nextTurnPlease = false;

                long trackGap = (long)((outerDiameter - innerDiameter)/2.0 - (turnsTotal*trackWidth))/(turnsTotal-1); // nm

		double radiusIncrementPerTurn = (trackWidth+trackGap);
		double radiusIncrementPerSegment = radiusIncrementPerTurn/(segmentsPerLoop);

		double x1 = 0;
		double y1 = 0;
		double x2 = 0;
		double y2 = 0;
		long layerNumber = 15; // front

		String moduleName = "";

		if (squareCoil)
		{
                        moduleName = turnsTotal + "_turn_square_spiral";
		}
		else
		{
			moduleName = turnsTotal + "_turn_spiral";
		}

		String outputFileName = "";
	
		if (theOneTrueEDAsuiteGEDA)
		{
			outputFileName = moduleName + ".fp";
		}
		else //kicad
		{
			outputFileName = moduleName + ".mod";
		}

		File outputFile = new File(outputFileName);

		PrintWriter footprintOutput = new PrintWriter(outputFile);

		String headerString = "";

		if (theOneTrueEDAsuiteGEDA)
		{
			headerString = headerString +
				("Element[\"\" \"Inductor\"" + 
				" \"\" \"\" 0 0 -1000 -1000 0 100 \"\"]" +
				"(\n");
		}
		else // kicad :-)
		{
                	headerString = headerString +
				"PCBNEW-LibModule-V1  mer 27 mar 2013 20:53:24 CET\n" +
                                "Units mm\n" +
                                "$INDEX\n" +
                                moduleName + "\n" +
                                "$EndINDEX\n" +
                                "$MODULE " + moduleName + "\n" +
                                "Po 0 0 0 15 51534DFF 00000000 ~~\n" +
                                "Li " + moduleName + "\n" +
                                "Cd " + moduleName + "\n" +
                                "Sc 0\n" +
                                "AR\n" +
                                "Op 0 0 0\n" +
                                "T0 0 -4134 600 600 0 120 N V 21 N \"S***\"\n";
//	                System.out.print(headerString);
		}

		footprintOutput.print(headerString);

                long currentLoopStartX = 0;
                long currentLoopStartY = 0;

                double trackWidthMM = trackWidth/1000.0;

		for (long spiralCounter = 0; spiralCounter < turnsTotal; spiralCounter++)
		{
			// 1mm segment lengths seem about right, but now user configurable
			// base it on outer loop
			// circumference = Math.pi * outerDiameter

			if (squareCoil)
			{
				currentLoopStartX = (long)(radiusIncrementPerTurn * spiralCounter);
				currentLoopStartY = (long)(radiusIncrementPerTurn * spiralCounter);
				// i.e. we step rightwards and down an increment for each new turn
				long squareSideLength = (long)((startRadius * 2) + (spiralCounter * 2.0 * radiusIncrementPerTurn));

				// we start the first side of the square, going up i.e. -ve Y direction
                                x1 = ((currentLoopStartX)/1000.0);
                                y1 = ((currentLoopStartY)/1000.0);
                                x2 = (currentLoopStartX/1000.0);
                                y2 = ((currentLoopStartY - squareSideLength)/1000.0);

                                // for gEDA we have to produce a pad description of the form
                                // Pad[X1 Y1 X2 Y2 Thickness Clearance Mask Name Number SFlags]
                                if (theOneTrueEDAsuiteGEDA)
                                {
                                        footprintOutput.format("Pad[%.3fmm %.3fmm %.3fmm %.3fmm", x1, y1, x2, y2);
                                        footprintOutput.format(" %.3fmm ", trackWidthMM);
                                        footprintOutput.print("0.254mm "); // the clearance is 10mil
                                        footprintOutput.print("0 "); // solder mask clearance is zero
                                        footprintOutput.print("\"A\" "); // name of coil
                                        footprintOutput.print("\"1\" "); // coil pad number
                                        footprintOutput.print("\"\"]\n"); // name of coil
                                }
                                else // kicad
                                {
                                // for kicad we have to produce
                                // a Draw Segment "DS" string of the form
                                // "DS x1 y1 x2 y2 thickness layer"
                                        footprintOutput.format("DS %.3f %.3f %.3f %.3f", x1, y1, x2, y2);
                                        footprintOutput.format(" %.3f ", trackWidthMM);
                                        footprintOutput.println(layerNumber);
                                }

				// we start the second side of the square, going left i.e. -ve X direction
                                x1 = (currentLoopStartX/1000.0);
                                y1 = ((currentLoopStartY - squareSideLength)/1000.0);
                                x2 = ((currentLoopStartX - squareSideLength)/1000.0);
                                y2 = ((currentLoopStartY - squareSideLength)/1000.0);

                                // for gEDA we have to produce a pad description of the form
                                // Pad[X1 Y1 X2 Y2 Thickness Clearance Mask Name Number SFlags]
                                if (theOneTrueEDAsuiteGEDA)
                                {
                                        footprintOutput.format("Pad[%.3fmm %.3fmm %.3fmm %.3fmm", x1, y1, x2, y2);
                                        footprintOutput.format(" %.3fmm ", trackWidthMM);
                                        footprintOutput.print("0.254mm "); // the clearance is 10mil
                                        footprintOutput.print("0 "); // solder mask clearance is zero
                                        footprintOutput.print("\"A\" "); // name of coil
                                        footprintOutput.print("\"1\" "); // coil pad number
                                        footprintOutput.print("\"\"]\n"); // name of coil
                                }
                                else // kicad
                                {
                                // for kicad we have to produce
                                // a Draw Segment "DS" string of the form
                                // "DS x1 y1 x2 y2 thickness layer"
                                        footprintOutput.format("DS %.3f %.3f %.3f %.3f", x1, y1, x2, y2);
                                        footprintOutput.format(" %.3f ", trackWidthMM);
                                        footprintOutput.println(layerNumber);
                                }

				// we start the third side of the square, going down i.e. +ve Y direction
                                x1 = ((currentLoopStartX - squareSideLength)/1000.0);
                                y1 = ((currentLoopStartY - squareSideLength)/1000.0);
                                x2 = ((currentLoopStartX - squareSideLength)/1000.0);
                                y2 = ((currentLoopStartY + radiusIncrementPerTurn)/1000.0);

                                // for gEDA we have to produce a pad description of the form
                                // Pad[X1 Y1 X2 Y2 Thickness Clearance Mask Name Number SFlags]
                                if (theOneTrueEDAsuiteGEDA)
                                {
                                        footprintOutput.format("Pad[%.3fmm %.3fmm %.3fmm %.3fmm", x1, y1, x2, y2);
                                        footprintOutput.format(" %.3fmm ", trackWidthMM);
                                        footprintOutput.print("0.254mm "); // the clearance is 10mil
                                        footprintOutput.print("0 "); // solder mask clearance is zero
                                        footprintOutput.print("\"A\" "); // name of coil
                                        footprintOutput.print("\"1\" "); // coil pad number
                                        footprintOutput.print("\"\"]\n"); // name of coil
                                }
                                else // kicad
                                {
                                // for kicad we have to produce
                                // a Draw Segment "DS" string of the form
                                // "DS x1 y1 x2 y2 thickness layer"
                                        footprintOutput.format("DS %.3f %.3f %.3f %.3f", x1, y1, x2, y2);
                                        footprintOutput.format(" %.3f ", trackWidthMM);
                                        footprintOutput.println(layerNumber);
                                }

				// we start the fourth side of the square, going right i.e. +ve X direction
                                x1 = ((currentLoopStartX - squareSideLength)/1000.0);
                                y1 = ((currentLoopStartY + radiusIncrementPerTurn)/1000.0);
                                x2 = ((currentLoopStartX + radiusIncrementPerTurn)/1000.0);
                                y2 = ((currentLoopStartY + radiusIncrementPerTurn)/1000.0);

                                // for gEDA we have to produce a pad description of the form
                                // Pad[X1 Y1 X2 Y2 Thickness Clearance Mask Name Number SFlags]
                                if (theOneTrueEDAsuiteGEDA)
                                {
                                        footprintOutput.format("Pad[%.3fmm %.3fmm %.3fmm %.3fmm", x1, y1, x2, y2);
                                        footprintOutput.format(" %.3fmm ", trackWidthMM);
                                        footprintOutput.print("0.254mm "); // the clearance is 10mil
                                        footprintOutput.print("0 "); // solder mask clearance is zero
                                        footprintOutput.print("\"A\" "); // name of coil
                                        footprintOutput.print("\"1\" "); // coil pad number
                                        footprintOutput.print("\"\"]\n"); // name of coil
                                }
                                else // kicad
                                {
                                // for kicad we have to produce
                                // a Draw Segment "DS" string of the form
                                // "DS x1 y1 x2 y2 thickness layer"
                                        footprintOutput.format("DS %.3f %.3f %.3f %.3f", x1, y1, x2, y2);
                                        footprintOutput.format(" %.3f ", trackWidthMM);
                                        footprintOutput.println(layerNumber);
                                }


			}
			else
			{ // start non square i.e. helical coil section
				while (!nextTurnPlease)
				{
					nextTheta = theta + deltaTheta;	
					nextRadius = startRadius + radiusIncrementPerSegment;
					// we figure out the coordinates in mm as double variables 
					// gEDA will recognise "XXX.XXmm" as mm
					x1 = ((startRadius * Math.cos(theta))/1000.0);
					y1 = ((startRadius * Math.sin(theta))/1000.0);
	      	                        x2 = ((nextRadius * Math.cos(nextTheta))/1000.0);
	                                y2 = ((nextRadius * Math.sin(nextTheta))/1000.0);

					// for gEDA we have to produce a pad description of the form
					// Pad[X1 Y1 X2 Y2 Thickness Clearance Mask Name Number SFlags]
					if (theOneTrueEDAsuiteGEDA)
					{
						footprintOutput.format("Pad[%.3fmm %.3fmm %.3fmm %.3fmm", x1, y1, x2, y2);
                        	                footprintOutput.format(" %.3fmm ", trackWidthMM);
						footprintOutput.print("0.254mm "); // the clearance is 10mil
						footprintOutput.print("0 "); // solder mask clearance is zero
						footprintOutput.print("\"A\" "); // name of coil
						footprintOutput.print("\"1\" "); // coil pad number
						footprintOutput.print("\"\"]\n"); // name of coil
					}
					else // kicad
					{
					// for kicad we have to produce
					// a Draw Segment "DS" string of the form
					// "DS x1 y1 x2 y2 thickness layer"
						footprintOutput.format("DS %.3f %.3f %.3f %.3f", x1, y1, x2, y2);
						footprintOutput.format(" %.3f ", trackWidthMM);
						footprintOutput.println(layerNumber);
					}
					startRadius = nextRadius;
					theta = nextTheta;
					if (theta > (2*Math.PI))
					{
//						System.out.println("Next Loop");
						theta = theta - (2.0*Math.PI);
						nextTurnPlease = true;
					}
				}
				nextTurnPlease = false;
			}// end non square coil if statement
		}

		if (theOneTrueEDAsuiteGEDA) // :-)
		{
			footprintOutput.println(")");
		}
		else // kicad
		{
                	footprintOutput.println("$EndMODULE " + moduleName);
		}

		// and we close the footprint file before finishing up
		footprintOutput.close();
	}

	private static void printUsage()
	{
		System.out.println("\nUsage:\n\n\t" +
			"java SpiralInductorFootprintGenerator -option value\n" +
			"\n\t\t-k\texport a kicad module, default is geda .fp file\n" +
                        "\n\t\t-s\texport a square inductor, default is circular\n" +
                        "\n\t\t-i long\t inner diameter of coil in nanometres\n" +
                        "\n\t\t-o long\t outer diameter of coil in nanometres\n" +
                        "\n\t\t-w long\t track width in nanometres\n" +
                        "\n\t\t-n long\t number of turns\n" +
			"\n\t\t-l long\t length of segment used to approximate circular arc in nanometres\n" +
			"\n\t\t-h\t prints this\n\n" +
			"Example usage:\n\n\t" +
			"java SpiralInductorFootprintGenerator -n 40 -i 15000 -o 50000 -w 250 -l 3000\n\n\t" +
			"generates a 40 turn helical inductor footprint,\n\t" +
			"of 15mm inside diameter, 50 mm outside diameter,\n\t" +
			"with 0.25mm track width and 3mm segment length.\n");
	}
}
