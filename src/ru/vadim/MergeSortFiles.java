package ru.vadim;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MergeSortFiles {

    private String dataType;
    private String sortType;
    private String outputFileName;
    private List<List<?>> allInputData;
    private List<?> allOutputData;
    Comparator comparator;

    MergeSortFiles(){};

    public void sort(String[] args) {
        prepareData(args);

        if (dataType.equals("-i")) {
            List<List<Integer>> allInputInteger = new ArrayList<>();
            for (List<?> inputList : allInputData) {
                allInputInteger.add(inputList.stream().map((a)->Integer.parseInt(a.toString())).collect(Collectors.toList()));
            }
            allOutputData = mergeSort(allInputInteger, dataType,comparator);
        }
        if(dataType.equals("-s")){
            List<List<String>> allInputString = new ArrayList<>();
            for (List<?> inputList : allInputData) {
                allInputString.add(inputList.stream().map((a)->(String)a).collect(Collectors.toList()));
            }
            allOutputData = mergeSort(allInputString, dataType,comparator);
        }

        writeToFile(allOutputData);

    }


    private void prepareData(String[] args){
        if(args[0].equals("-a")||args[0].equals("-d") && (args.length >= 5)){
            sortType = args[0];
            dataType = args[1];
            outputFileName = args[2];
            allInputData = readFile(dataType,Arrays.copyOfRange(args,3,args.length));

        } else if(args[0].equals("-s")||args[0].equals("-i") && (args.length >=4)){
            sortType = "-a";
            dataType = args[0];
            outputFileName = args[1];
            allInputData = readFile(dataType,Arrays.copyOfRange(args,2,args.length));

        } else {
            throw new IllegalArgumentException("Введите правильные аргументы");
        }

        comparator = getComparator(dataType,sortType);
    }

    private List<List<?>> readFile(String dataType,String[] inputFileNames) {

        List<List<?>> inputList = new ArrayList<>();

        for (String fileName : inputFileNames) {
            try {
                if(dataType.equals("-i")){
                    List<?> inputFileData = Files.lines(Paths.get(fileName)).map(Integer::parseInt).collect(Collectors.toList());
                    inputList.add(inputFileData);
                }
                if(dataType.equals("-s")){
                    List<?> inputFileData = Files.lines(Paths.get(fileName)).collect(Collectors.toList());
                    inputList.add(inputFileData);
                }
            } catch (FileNotFoundException e) {
                System.out.println("не найден файл  " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                System.out.println("Одним из элементов файла оказался не того формата: " + e);
            }
        }
        return inputList;
    }

    private <T> List<T> mergeSort(List<List<T>> inputData, String dataType, Comparator comparator) {

        List<T> resultList = new ArrayList<>();
        List<T> firstList = inputData.get(0);
        List<T> secondList = inputData.get(1);

        int l = 1;
        do {
            resultList.clear();

            int i = 0, j = 0;

            T previousElemFirstList = firstList.get(0);
            T previousElemSecondList = secondList.get(0);
            while (i < firstList.size() && j < secondList.size()) {

                if(comparator.compare(previousElemFirstList,firstList.get(i))>0){
                    System.out.println("не отсортирован исходный файл №"+ l +" , не отсортированный элемент - "+ firstList.get(i) +" будет пропушен");
                    if (i < firstList.size()) { i++;}
                }
                if(comparator.compare(previousElemSecondList,secondList.get(j))>0){
                    System.out.println("не отсортирован исходный файл №"+ (l+1) +" , не отсортированный элемент - "+ secondList.get(j) +" будет пропушен");
                    if (j < secondList.size()){j++;}
                }

                previousElemFirstList = firstList.get(i);
                previousElemSecondList = secondList.get(j);
                if (comparator.compare(firstList.get(i),secondList.get(j)) < 0) {
                    resultList.add(firstList.get(i));
                    i++;
                } else {
                    resultList.add(secondList.get(j));
                    j++;
                }

            }

            if (i < firstList.size()) {
                resultList.addAll(firstList.subList(i, firstList.size()));
            }
            if (j < secondList.size()) {
                resultList.addAll(secondList.subList(j, secondList.size()));
            }

            l++;
            if (inputData.size() > l) {
                firstList.clear();
                firstList.addAll(resultList);
                secondList = inputData.get(l);
            }
        } while (l < inputData.size());

        return resultList;
    }

    private <T> void writeToFile(List<T> allData){
        try {
            if (dataType.equals("-i")) {
                List<String> outputInteger = new ArrayList<>();
                for (T line : allData) {
                    outputInteger.add(line.toString());
                }
                Files.write(Paths.get(outputFileName),  outputInteger);
            }
            if(dataType.equals("-s")){
                List<String> outputString = new ArrayList<>();
                for (T line : allData) {
                    outputString.add((String)line);
                }
                Files.write(Paths.get(outputFileName),  outputString);
            }
        } catch (FileNotFoundException e) {
            System.out.println("не найден файл для записи результатов " + outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Comparator getComparator(String dataType, String typeSort) {
        if (dataType.equals("-i")) {
            return new CompareInteger(typeSort);
        }
        if (dataType.equals("-s")) {
            return new CompareString(typeSort);
        } else {
            throw new RuntimeException("Неправильный тип сортировки");
        }
    }

    static class CompareInteger implements Comparator<Integer> {
            private final String typeSort;

            CompareInteger(String typeSort) {
                this.typeSort = typeSort;
            }

            @Override
            public int compare(Integer element1, Integer element2) {
                if (typeSort.equals("-d")) {
                    return element2.compareTo(element1);
                } else {
                    return element1.compareTo(element2);
                }
            }
    }

    static class CompareString implements Comparator<String> {
            private final String typeSort;

            CompareString(String typeSort) {
                this.typeSort = typeSort;
            }

            @Override
            public int compare(String element1, String element2) {
                if (typeSort.equals("-d")) {
                    return element2.compareTo(element1);
                } else {
                    return element1.compareTo(element2);
                }
            }
        }

}
