/***********************************************************
* Developer: Minhas Kamal (minhaskamal024@gmail.com)       *
* Website: https://github.com/MinhasKamal/Intellectron     *
* License:  GNU General Public License version-3           *
***********************************************************/

package com.minhaskamal.deepGenderRecognizer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import com.minhaskamal.egami.matrix.Matrix;
import com.minhaskamal.egami.matrixUtil.MatrixUtilities;
import com.minhaskamal.intellectron.MultiLayerNeuralNetworkImplementation;

public class DeepGenderRecognizer {
	public static void main(String[] args) throws Exception {
		System.out.println("OPERATION STARTED!!!");
		
		
		//prepare data//
		System.out.println("PREPARING DATA...");
		String rootPath = "src/res/img";
		int matrixHeight = 25;
		int matrixWidth = 25;
		String[][] allFilePaths = prepareDataPaths(rootPath);
		double[][][] inputs = prepareMatrixData(allFilePaths);	//[folder][file][data] i.e. [male][001.png][125,233,...]
		
		double[][] outputs = new double[inputs.length][];
		for(int i=0; i<outputs.length; i++){
			outputs[i] = new double[]{i};
		}
		
		
		/**/
		//train//
		System.out.println("TRAINING NETWORK...");
		int[] numbersOfNeuronsInLayers = new int[]{20, 5, 1};
		MultiLayerNeuralNetworkImplementation neuralNetworkImplementation = new MultiLayerNeuralNetworkImplementation(numbersOfNeuronsInLayers,
				0.1, matrixHeight*matrixWidth);
		neuralNetworkImplementation = train(neuralNetworkImplementation, inputs, outputs);
		/**/
		
		/**/
		//store//
		String workspace = System.getenv("SystemDrive") + System.getenv("HOMEPATH") + "\\Desktop\\";
		neuralNetworkImplementation.dump(workspace+"knowledge.xml");
		
		//load//
//		NeuralNetworkImplementation neuralNetworkImplementation = new NeuralNetworkImplementation(workspace+"know.xml");
		/**/
		
		/**/
		//predict//
		System.out.println("PREDICTING...");
		predict(neuralNetworkImplementation, inputs);
		/**/
		
		/*/
		//generate//
		Matrix matrix = createMatrix(neuralNetworkImplementation.generate(new double[]{0.591, 0.51}));
		matrix.write(workspace+"pic.png");
		/**/
	}
	
	//////////////////////////////////////////////////////////////////////////////
	
	public static MultiLayerNeuralNetworkImplementation train(
			MultiLayerNeuralNetworkImplementation neuralNetworkImplementation,
			double[][][] inputs, double[][] outputs){
		
		int cycle=30;
		for(int c=0; c<cycle; c++){
			for(int j=0; j<1000; j++){
				for(int i=0; i<inputs.length; i++){
					neuralNetworkImplementation.train(inputs[i][j], outputs[i]);
				}
			}
			System.out.println("Cycle- " + c);
		}
		
		return neuralNetworkImplementation;
	}
	
	public static void predict(
			MultiLayerNeuralNetworkImplementation neuralNetworkImplementation,
			double[][][] inputs){
		
		for(int i=0; i<2; i++){
			if(i==0){
				System.out.println("##Female##");
			}else{
				System.out.println("##Male##");
			}
			for(int c=0; c<100; c++){
				double[] out = neuralNetworkImplementation.predict(inputs[i][1000+c]);
				if(out[0]<0.5){
					System.out.println("female");
				}else{
					System.out.println("male");
				}
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////
	
	public static int[] vectorize(Matrix matrix){
		int height = matrix.getRows();
		int width = matrix.getCols();
		
		int[] vector = new int[height*width];
		
		for(int i=0, k=0; i<height; i++){
			for(int j=0; j<width; j++){
				vector[k] = matrix.pixels[i][j][0];
				k++;
			}
		}
		
		return vector;
	}
	
	public static double[] scale(int[] vector, int minValue, int maxValue){
		double[] scaledVector = new double[vector.length];
		
		for(int i=0; i<vector.length; i++){
			scaledVector[i] = (vector[i]-minValue)/(maxValue-minValue);
		}
		
		return scaledVector;
	}
	
	public static Matrix createMatrix(double[] vector){ //creates square matrix
		int row = (int) Math.sqrt(vector.length);
		int col = row;
		
		Matrix matrix = new Matrix(row, col, Matrix.BLACK_WHITE);
		
		int k=0;
		for(int i=0; i<row; i++){
			for(int j=0; j<col; j++){
				matrix.pixels[i][j] = new int[]{ (int) (vector[k]*254) };
				k++;
			}
		}
		
		return matrix;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////
	
	public static String[][] prepareDataPaths(String rootFolderPath){
		File[] directories = new File(rootFolderPath).listFiles(new FileFilter() {
			@Override
			public boolean accept(File arg0) {
				return arg0.isDirectory();
			}
		});
		
		String[][] allFilePaths = new String[directories.length][];
		for(int i=0; i<directories.length; i++){
			File[] files = directories[i].listFiles();
			allFilePaths[i] = new String[files.length];
			
			for(int j=0; j<files.length; j++){
				allFilePaths[i][j] = files[j].getAbsolutePath();
			}
		}
		
		return allFilePaths;
	}
	
	public static double[][][] prepareMatrixData(String[][] allFilePaths){
		double[][][] inputs = new double[allFilePaths.length][][];
		
		try {
			for(int i=0; i<allFilePaths.length; i++){
				inputs[i] = new double[allFilePaths[i].length][];
				for(int j=0; j<allFilePaths[i].length; j++){
					Matrix matrix;
						matrix = new Matrix(allFilePaths[i][j], Matrix.BLACK_WHITE);
					matrix = new MatrixUtilities().convertToBinary(matrix, 170);
					int[] rawInputVector = vectorize(matrix);
					inputs[i][j] = scale(rawInputVector, 0, 255);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return inputs;
	}
}
