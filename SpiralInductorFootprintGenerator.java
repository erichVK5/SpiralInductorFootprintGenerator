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
//    along with this program if not, write to the Free Software
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

		long innerDiameter = 27100; // microns
                long outerDiameter = 32000; // microns
                long segmentLength = 1000; // 1mm length in microns
                int turnsTotal = 9;
                long trackWidth = 220; // microns
                        // (((outerDiameter-innerDiameter)/2-(turnsTotal-1)*trackGap))/turnsTotal;

                boolean theOneTrueEDAsuiteGEDA = true;
                	// obviously, !theOneTrueEDAsuiteGEDA = kicad :-)

		int vertices = 0;

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
			else if (args[counter].startsWith("-v"))
			{
				vertices = Integer.parseInt(args[counter].substring(2));
				if ((vertices == 1) || (vertices == 2))
				{
					System.out.println("Assuming inductor is helical.");
					vertices = 0;
				}
			}
                        else 
                        {
                                printUsage();
				System.exit(0);
                        }
			
		}

		// some basic preliminaries for all scenarios

                double startRadius = (innerDiameter + trackWidth)/2.0;
		double nextRadius = startRadius;

		// now some preliminaries for heliical inductors
                // we now sort out appropriate angular step sizes for the loops and
                // the loop spacings based on the inner and outer dimensions given

                double theta = 0;
                double nextTheta = 0;

		// we figure out the circumference, well, at least a reasonable
		// approximation of a real number using the set of long integers
		long circumference = (long)Math.PI * outerDiameter;

		// we base segments per loop on the outermost loop circumference
		double segmentsPerLoop = Math.PI*outerDiameter/segmentLength;
		// we figure out a step size in radians to step around the spiral
                // which is 2pi radians divided by number of segments
		double deltaTheta = (2.0 * Math.PI)/segmentsPerLoop;

		// we now define some flags
		boolean nextTurnPlease = false;

                // none of the above prelimiaries are needed for n-gons

		long trackGap = 0; // in microns

		if (turnsTotal > 1) // the usual scenario
		{
                	trackGap = (long)((outerDiameter - innerDiameter)/2.0 - (turnsTotal*trackWidth))/(turnsTotal-1); // nm
		}
		else // stops a divide by zero error if only one loop requested
		{
			trackGap = (long)(innerDiameter/2.0); // i.e. startRadius
		}

		double radiusIncrementPerTurn = (trackWidth+trackGap);
		double radiusIncrementPerSegment = radiusIncrementPerTurn/(segmentsPerLoop);

		// we use x1,y1,x2,y2 as variables for the begining and end coords of line segments
		double x1 = 0;
		double y1 = 0;
		double x2 = 0;
		double y2 = 0;
                // we use x1scales,y1scaled,x2scaled,y2scaled as variables for
		// the beginning and end coords of scaled helical coil segments
		// for capacitance length calculation
                double x1scaled = 0;
                double y1scaled = 0;
                double x2scaled = 0;
                double y2scaled = 0;

		long layerNumber = 15; // front for kicad

		String moduleName = "";

		switch (vertices) // vertices=1 and vertices=2 were screened out during args parsing
		{
			case 0:	moduleName = turnsTotal + "_turn_helical_inductor";
				break;
			case 3: moduleName = turnsTotal + "_turn_triangular_inductor";
                                break;
			case 4: moduleName = turnsTotal + "_turn_square_inductor";
				break;
			case 5:	moduleName = turnsTotal + "_turn_pentagonal_inductor";
                                break;
			case 6: moduleName = turnsTotal + "_turn_hexagonal_inductor";
                                break;
			case 7: moduleName = turnsTotal + "_turn_heptagonal_inductor";
                                break;
                        case 8: moduleName = turnsTotal + "_turn_octagonal_inductor";
                                break;
                        case 9: moduleName = turnsTotal + "_turn_nonagonal_inductor";
                                break;
                        case 10: moduleName = turnsTotal + "_turn_decagonal_inductor";
                                break;
			default: moduleName = turnsTotal + "_turn_" + vertices + "_gon_inductor";
				break;
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

		System.out.println("Generating " + turnsTotal + " turn inductor:" +
			outputFileName);

		System.out.println("Using track gap of: " + trackGap + " microns.");
		System.out.println("Using track width of: " + trackWidth + " microns.");

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
		}

		footprintOutput.print(headerString);

                long currentLoopStartX = 0;
                long currentLoopStartY = 0;

                double trackWidthMM = trackWidth/1000.0;
		double trackGapMM = trackGap/1000.0;

		// we need to calculate the effective length of the distributed capacitor
                double cumulativeCapacitorLengthMM = 0.0;

		for (long spiralCounter = 0; spiralCounter < turnsTotal; spiralCounter++)
		{

			if (vertices != 0) // we are making an n-gon, as opposed to a helical coil
			{
				// the following if then else structure figures out a starting theta
				// for the n-gon in an attempt to give an aesthetically pleasing coil
				if ((vertices % 2) == 1)
				{
					theta = (Math.PI/(2 * vertices));
				}
                                else if ((vertices % 2) == 0) 
                                {
                                        theta = (Math.PI/(vertices));
                                }
				else
				{
					theta = 0.0;
				}

				// we figure out the radius at a vertex using some trigonometry
				nextRadius = startRadius/Math.cos(Math.PI/vertices) + (spiralCounter * (radiusIncrementPerTurn/Math.cos(Math.PI/vertices)));

				// we step through, one vertex after another, until we complete a turn
				for (int vertexCount = 0; vertexCount < vertices; vertexCount++)
				{
					if (vertexCount < (vertices -2))
					{
	                        	        x1 = (nextRadius * Math.cos(vertexCount * 2 * Math.PI/vertices + theta))/1000.0;
	                        	        y1 = (nextRadius * Math.sin(vertexCount * 2 * Math.PI/vertices + theta))/1000.0;
	                        	        x2 = ((nextRadius * Math.cos((vertexCount + 1) * 2 * Math.PI/vertices + theta))/1000.0);
	                        	        y2 = ((nextRadius * Math.sin((vertexCount + 1) * 2 * Math.PI/vertices + theta))/1000.0);
					}
					else if (vertexCount == (vertices-2))
                                        {
                                                x1 = (nextRadius * Math.cos(vertexCount * 2 * Math.PI/vertices + theta))/1000.0;
                                                y1 = (nextRadius * Math.sin(vertexCount * 2 * Math.PI/vertices + theta))/1000.0;
                                                x2 = ((nextRadius * Math.cos((vertexCount + 1) * 2 * Math.PI/vertices + theta))/1000.0);
                                                y2 = ((nextRadius * Math.sin((vertexCount + 1) * 2 * Math.PI/vertices + theta))/1000.0);
                                                // the second to last line segment making up the n-gon
                                                // need to be lengthened to allow the final segment of
                                                // of the current turn to finish where the next turn
                                                // starts
						// the segment is lengthened by an amount equal to 
						// = radiusIncrementPerTurn/cos(90-360/vertices)
						// = radiusIncrementPerTurn/ sin(360/vertices)
						// and the deltaX and deltaY components of the
						// segment are lengthened proportionally

						x2 = x1 + ((x2 - x1) * (calculateSegmentLength(x1, y1, x2, y2) + (radiusIncrementPerTurn/(Math.sin(2.0 * Math.PI/vertices) * 1000)) )/calculateSegmentLength(x1, y1, x2, y2));
                                                y2 = y1 + ((y2 - y1) * (calculateSegmentLength(x1, y1, x2, y2) + (radiusIncrementPerTurn/(Math.sin(2.0 * Math.PI/vertices) * 1000)) )/calculateSegmentLength(x1, y1, x2, y2));
						// we just scaled x2, y2 to match the next turn
                                        }
                                        else // last segment of current loop
                                        {
						// for the final segment of the current turn, we
						// increment the radius to match the next turn
                                                nextRadius = startRadius/Math.cos(Math.PI/vertices)
                                                        + ((spiralCounter + 1) * (radiusIncrementPerTurn/Math.cos(Math.PI/vertices)));
						// we can start where we left off with the last
						// line segment
                                                x1 = x2; // copy the previous coords
                                                y1 = y2; // copy the previous coords
						// and we finish the segment using the new radius
						// that was incremented to match the next turn
                                                x2 = ((nextRadius * Math.cos((vertexCount + 1) * 2 * Math.PI/vertices + theta))/1000.0);
                                                y2 = ((nextRadius * Math.sin((vertexCount + 1) * 2 * Math.PI/vertices + theta))/1000.0);
                                        }

					// we only have capacitance between turns, so we stop
					// summing capacitance length when generating the
					// final turn, i.e. stop at (turnsTotal - 1)

	                                if (spiralCounter < (turnsTotal - 1))
	                                {
	                                        cumulativeCapacitorLengthMM +=
	                                                calculateSegmentLength(x1, y1, x2, y2)
	                                                + (radiusIncrementPerTurn * Math.tan(Math.PI/vertices));
	                                }

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
			} // end n-gon IF statement 

			else if (vertices == 0) // not an n-gon, it is a helical coil
			{
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

					// we numerically integrate the length of the midline
					// between turns, hence the use of the + (trackGap/2.0)
					// to estabish the midline of the gap

                                        x1scaled = ((startRadius + (trackGap/2.0))* Math.cos(theta))/1000.0;
                                        y1scaled = ((startRadius + (trackGap/2.0))* Math.sin(theta))/1000.0;
                                        x2scaled = ((nextRadius + (trackGap/2.0))* Math.cos(nextTheta))/1000.0;
                                        y2scaled = ((nextRadius + (trackGap/2.0))* Math.sin(nextTheta))/1000.0;


					// there is only capacitance between turns, so we stop summing
					// capacitor length at (turnsTotal -1)

	                                if (spiralCounter < (turnsTotal - 1))
        	                        {
                	                        cumulativeCapacitorLengthMM +=
                        	     calculateSegmentLength(x1scaled, y1scaled, x2scaled, y2scaled);
                                	}

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
						theta = theta - (2.0*Math.PI);
						nextTurnPlease = true;
					}
				}
				nextTurnPlease = false;
			}// end helical coil IF statement
		}

		if (theOneTrueEDAsuiteGEDA) // :-)
		{
			footprintOutput.println(")");
		}
		else // kicad
		{
                	footprintOutput.println("$EndMODULE " + moduleName);
		}

		System.out.println("Outer diameter of coil (mm): " + (outerDiameter)/1000.0);
		System.out.println("Inner diameter of coil (mm): " + (innerDiameter)/1000.0);
		if (vertices != 0)
		{
			System.out.println("Inductor has " + vertices + " vertices.");
		}
		else
		{
			System.out.println("Inductor is helical");
		}

 		System.out.print("Total capacitor length (mm): ");
		System.out.format("%.4f\n", cumulativeCapacitorLengthMM);

		double finalCapacitanceF = calculateCapacitance(trackGapMM,cumulativeCapacitorLengthMM);
		
		System.out.println("Total calculated capacitance (F): " + finalCapacitanceF);
                System.out.print("Total calculated capacitance (pF): ");
		System.out.format("%.4f\n", (finalCapacitanceF*1E12));

                // the following variables are used to calculate inductance
                // using the "Greenhouse" equation for flat "pancake" inductors
                // we set the default values to those needed for a helical coil
                double greenhouseC1 = 1.00; // Square 1.27, Hexagonal 1.09, Circle 1.00
                double greenhouseC2 = 2.46; // Square 2.07, Hexagonal 2.23, Circle 2.46
                double greenhouseC3 = 0.00; // Square 0.18, Hexagonal 0.00, Circle 0.00
                double greenhouseC4 = 0.20; // Square 0.13, Hexagonal 0.17, Circle 0.20

		if (vertices == 4)
		{
                	greenhouseC1 = 1.27; // Square 1.27, Hexagonal 1.09, Circle 1.00
                	greenhouseC2 = 2.07; // Square 2.07, Hexagonal 2.23, Circle 2.46
                	greenhouseC3 = 0.18; // Square 0.18, Hexagonal 0.00, Circle 0.00
                	greenhouseC4 = 0.13; // Square 0.13, Hexagonal 0.17, Circle 0.20
		}
		else if (vertices == 6)
		{
                	greenhouseC1 = 1.09; // Square 1.27, Hexagonal 1.09, Circle 1.00
                	greenhouseC2 = 2.23; // Square 2.07, Hexagonal 2.23, Circle 2.46
                	greenhouseC3 = 0.00; // Square 0.18, Hexagonal 0.00, Circle 0.00
                	greenhouseC4 = 0.17; // Square 0.13, Hexagonal 0.17, Circle 0.20
		}
		else if (vertices == 8)
                {
                        greenhouseC1 = 1.07; // Square 1.27, Hexagonal 1.09, Circle 1.00
                        greenhouseC2 = 2.29; // Square 2.07, Hexagonal 2.23, Circle 2.46
                        greenhouseC3 = 0.00; // Square 0.18, Hexagonal 0.00, Circle 0.00
                        greenhouseC4 = 0.19; // Square 0.13, Hexagonal 0.17, Circle 0.20
                }
		else if (vertices != 0)
		{
			System.out.println("Using inductance equation for circle due to a" +
					" lack of published parameters\nfor the inductance of " +
					vertices + " vertex inductors.");
		}


		double finalInductanceH = calculateInductance(turnsTotal, innerDiameter, outerDiameter, greenhouseC1, greenhouseC2, greenhouseC3, greenhouseC4);
		
 		System.out.println("Calculated inductance (Henries): " + finalInductanceH);
                System.out.print("Calculated inductance (uH): ");
                System.out.format("%.4f\n", (finalInductanceH*1000000));
		System.out.print("Calculated self resonant frequency (Hz): ");
		System.out.format("%.0f\n", calculateSelfResonance(finalInductanceH, finalCapacitanceF));
                System.out.print("Calculated self resonant frequency (MHz): ");
		System.out.format("%.4f\n", calculateSelfResonance(finalInductanceH, finalCapacitanceF)/1E6);
		// and we close the footprint file before finishing up
		footprintOutput.close();
	}

	private static double calculateSelfResonance(double inductanceHenries, double capacitance)
	{
                // method employed described in http://dx.doi.org/10.4236/cs.2013.42032
                // "Design and Optimization of Printed Circuit Board
                // Inductors for Wireless Power Transfer System" by
                // Ashraf B. Islam, Syed K. Islam, Fahmida S. Tulip
                // Circuits and Systems, 2013, 4, 237-244

		// we use frequency = 1/(2*pi*sqrt(LC)) 
		return (1.0/(2.0*Math.PI*Math.sqrt(inductanceHenries*capacitance)));
	}

	private static double calculateSegmentLength(double xOne, double yOne, double xTwo, double yTwo)
	{
		double lengthSquared = ((xOne - xTwo) * (xOne - xTwo))+((yOne - yTwo) * (yOne - yTwo));
		return Math.sqrt(lengthSquared);
	}

	private static double calculateCapacitance(double trackGapMilliM, double gapLengthMilliM)
	{
                // method employed described in http://dx.doi.org/10.4236/cs.2013.42032
                // "Design and Optimization of Printed Circuit Board
                // Inductors for Wireless Power Transfer System" by
                // Ashraf B. Islam, Syed K. Islam, Fahmida S. Tulip
                // Circuits and Systems, 2013, 4, 237-244
		double etaRC = 3.1; // solder mask relative permittivity a.k.a. dielectric constant
		double etaRS = 4.7; // approx, fibreglass relative permittivity (dielectric constant)
				// etaRA = 1.006 for air at STP at ~ 0.9MHz
		double alpha = 0.9; // for FR4 coating
		double beta = 0.1; // for FR4 substrate	
		double eta0 = 8.854E-12; // dielectric constant of a vacuum	
		double copperThicknessM = 0.00003556; // in metres = 35.56 microns for 1oz/ft^2 copper
		double trackGapM = trackGapMilliM/1000.0; // convert mm to metres
		double gapLengthM = gapLengthMilliM/1000.0; // convert mm to metres
		double calculatedCapacitance = (alpha*etaRC + beta*etaRS)*eta0*copperThicknessM*gapLengthM/trackGapM;
		// i.e. the formula for parallel plates of a capacitor
		//            = (plateArea/gap)*dielectricConstantOfVacuum*relativePermittivity
		return calculatedCapacitance;
	}

	private static double calculateInductance(int turns, long dIn, long dOut, double c1, double c2, double c3, double c4)
	{
		// method employed described in http://dx.doi.org/10.4236/cs.2013.42032
		// "Design and Optimization of Printed Circuit Board
		// Inductors for Wireless Power Transfer System" by
		// Ashraf B. Islam, Syed K. Islam, Fahmida S. Tulip
		// Circuits and Systems, 2013, 4, 237-244
		double dAvg = ((dOut + dIn)/1000000.0)/2.0; // convert distance in microns to metres
		double sigma = (dOut - dIn)/(1.0*(dOut + dIn)); // sigma = "coil fill ratio"
		double mu = 4*Math.PI/10000000; // vacuum permeability = 4*pi * 10^(-7)
		double inductance = 0;
		inductance = ((mu * turns * turns * dAvg * c1)/2.0)*(Math.log(c2/sigma) + c3*sigma + c4*sigma*sigma);
		return inductance; // in Henries (H)
	}

	private static void printUsage()
	{
		System.out.println("\nUsage:\n\n\t" +
			"java SpiralInductorFootprintGenerator -option value\n" +
			"\n\t\t-k\texport a kicad module, default is geda .fp file\n" +
                        "\n\t\t-vN\texport an N-gonal inductor instead of default helical inductor\n" +
			"\n\t\t\ti.e. -v3 for triangle, -v4 for square, -v6 for hexagon\n" + 
                        "\n\t\t-i long\t inner diameter of coil in microns\n" +
                        "\n\t\t-o long\t outer diameter of coil in microns\n" +
                        "\n\t\t-w long\t track width in microns\n" +
                        "\n\t\t-n long\t number of turns\n" +
			"\n\t\t-l long\t length of segment used to approximate circular arc in microns\n" +
			"\n\t\t-h\t prints this\n\n" +
			"Example usage:\n\n\t" +
			"java SpiralInductorFootprintGenerator -n 40 -i 15000 -o 50000 -w 250 -l 3000\n\n\t" +
			"generates a 40 turn helical inductor footprint,\n\t" +
			"of 15mm inside diameter, 50 mm outside diameter,\n\t" +
			"with 0.25mm track width and 3mm segment length.\n");
	}
}
