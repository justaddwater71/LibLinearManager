package edu.nps.LibLinearManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.StringTokenizer;

import liblinear.InvalidInputDataException;
import liblinear.Linear;
import liblinear.Model;
import liblinear.Parameter;
import liblinear.Problem;
import liblinear.SolverType;
import liblinear.Train;
import liblinear.Predict;

public class LibLinearManager 
{
	//Data Members
	public static final String FILE_DELIM					= System.getProperty("file.separator");
	public static final String TRAIN_DIR_NAME 		= "train";
	public static final String MODEL_DIR_NAME		= "model";
	public static final String PREDICT_DIR_NAME 	= "predict";
	public static final String RESULT_DIR_NAME 	= "result";
	
	//Constructors
	
	
	//Methods
	public static void dummyPredictDirectory(File parentDirectory) throws FileNotFoundException, IOException
	{
		File predictDirectory = new File(parentDirectory, PREDICT_DIR_NAME);
		File resultDirectory = new File(parentDirectory, RESULT_DIR_NAME);
		
		dummyPredictDirectory(predictDirectory, resultDirectory);
	}
	
	public static void dummyPredictDirectory(File predictDirectory, File resultDirectory) throws FileNotFoundException, IOException
	{
		File[] predictArray = predictDirectory.listFiles();
		File predictFile;
		File resultFile;
		
		for (int i=0; i < predictArray.length; i++)
		{
			predictFile = predictArray[i];
			resultFile = new File(resultDirectory, predictFile.getName());
			
			dummyPredictFile(predictFile, resultFile);
		}
		
	}
	
	public static void dummyPredictFile(File predictFile, File resultFile) throws FileNotFoundException, IOException
	{
		BufferedReader predictReader = new BufferedReader( new FileReader(predictFile));
		PrintWriter resultPrintWriter;
		String predictString;
		
		try
		{
			resultPrintWriter = new PrintWriter(resultFile);
		}
		catch(IOException i)
		{
			resultFile.getParentFile().mkdirs();
			resultFile.createNewFile();
			resultPrintWriter = new PrintWriter(resultFile);
		}

		String currentLine;
		StringTokenizer tokenizer;
		
		while((currentLine = predictReader.readLine()) != null)
		{
			tokenizer = new StringTokenizer(currentLine);
			
			predictString = tokenizer.nextToken();
			
			resultPrintWriter.println(predictString);
		}
		
		resultPrintWriter.flush();
	}
	
	public static void TrainArray(File[] fileArray, long bias) throws IOException, InvalidInputDataException
	{
		for (int i=0; i < fileArray.length; i++)
		{
			TrainFile(fileArray[i], bias);
		}
	}
	
	public static void TrainDirectory(File dir, long bias) throws IOException, InvalidInputDataException
	{
		File[] fileArray;
		
		if (dir.isDirectory())
		{
			fileArray = dir.listFiles();
			
			for (int i=0; i < fileArray.length; i++)
			{
				TrainDirectory(fileArray[i], bias);
			}
		}
		else
		{
			TrainFile(dir, bias);
		}
	}
	
/*	public static void TrainFile(File trainFile, long bias) throws IOException, InvalidInputDataException
	{
		//Create/verify model path
		//String trainPath 	= trainFile.getAbsolutePath();
		
		File parentDirectory = trainFile.getParentFile().getParentFile();
		
		File modelDir = new File(parentDirectory, MODEL_DIR_NAME);
		
		File modelFile = new File(modelDir, trainFile.getName());
		
		TrainFile(trainFile, modelFile, bias);
	}*/

	public static Model TrainFile(File trainFile, long bias) throws IOException, InvalidInputDataException
	{
		//FIXME Get rid of hardwire SolverType after initial testing
		Parameter parameter = new Parameter(SolverType.MCSVM_CS, 1, Double.POSITIVE_INFINITY);
		Problem problem = Train.readProblem(trainFile, bias);
		Model model = Linear.train(problem, parameter);
		
		return model;
	}
	
	public static void TrainFile(File trainFile, File modelFile,  long bias) throws IOException, InvalidInputDataException
	{
		try
		{
			modelFile.createNewFile();
		}
		catch (IOException i)
		{
			modelFile.getParentFile().mkdirs();
			modelFile.createNewFile();
		}
		//FIXME Get rid of hardwire SolverType after initial testing
		Parameter parameter = new Parameter(SolverType.L2R_L2LOSS_SVC_DUAL, 1, Double.POSITIVE_INFINITY);
		Problem problem = Train.readProblem(trainFile, bias);
		Model model = Linear.train(problem, parameter);
		Linear.saveModel(modelFile, model);
	}
	
	public static void PredictFile(File predictFile, Model model, File resultFile) throws IOException, InvalidInputDataException
	{
		try
		{
			resultFile.createNewFile();
		}
		catch (IOException i)
		{
			resultFile.getParentFile().mkdirs();
			resultFile.createNewFile();
		}
		
        BufferedReader reader = null;
        Writer writer = null;
       /*NOTE: Had to explicity state Public on Linear.FILE_CHARSET and Predict.doPredict despite the
        * fact that there was no public nor private tag on either of these.  I understand that in a public
        * class, any untagged method or data member is public by default.  IF an outside liblinear is
        * pulled into this project, the project COULD break.  Just keep that in mind.*/
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(predictFile), Linear.FILE_CHARSET));
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile), Linear.FILE_CHARSET));
            
            Predict.doPredict(reader, writer, model);
            
            reader.close();
            writer.close();
	}
	
	public static void PredictFile(File predictFile, File modelFile, File resultFile) throws IOException, InvalidInputDataException
	{
		Model model = Linear.loadModel(modelFile);
		
		PredictFile(predictFile, model, resultFile);
	}
	
	public static void PredictFile(File predictFile, File modelFile) throws IOException, InvalidInputDataException
	{
		File resultDir = new File(predictFile.getParentFile(), RESULT_DIR_NAME);
		File resultFile = new File(resultDir, predictFile.getName());
		
		PredictFile(predictFile, modelFile, resultFile);
	}
	
	public static void run(File baseDir, String baseFileName, long bias) throws IOException, InvalidInputDataException
	{
		File trainFile 		= new File(baseDir, TRAIN_DIR_NAME 	+ FILE_DELIM + baseFileName);
		//File modelFile	= new File(baseDir, MODEL_DIR_NAME 	+ FILE_DELIM + baseFileName);
		File predictFile 	= new File(baseDir, PREDICT_DIR_NAME	+ FILE_DELIM + baseFileName);
		File resultFile 	= new File(baseDir, RESULT_DIR_NAME 	+ FILE_DELIM + baseFileName);
		
		Model model = TrainFile(trainFile, bias);
		PredictFile(predictFile, model, resultFile);
	}
	
	public static void main(String[] args) throws IOException, InvalidInputDataException 
	{
		String baseDirPath = args[0];
		
		long bias = Long.parseLong(args[2]);
		
		String baseFileName = args[1];
		
		File baseDir = new File(baseDirPath);
		
		run(baseDir, baseFileName, bias);
	}

}
