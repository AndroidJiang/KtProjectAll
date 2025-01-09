package com.example.ktprojectall.kotlin.sort;

public class Sort {
    public static void main(String[] args) {
        int[] arr = new int[]{4,2,1,6,3,8,10,0,5,99};
//        bubuleSort(arr);
//        chooseSort(arr);
        insertSort(arr);
        for (int i : arr) {
            System.out.print(i+",");
        }
    }

    /**
     * 冒泡排序
     * @param arr
     */
    public static void bubuleSort(int[] arr){
        for(int i=0;i< arr.length;i++){
            for(int j=1;j<arr.length-i-1;j++){
                if(arr[j-1]>arr[j]){
                    swap(arr,j-1,j);
                }
            }
        }
    }

    /**
     * 选择排序
     * @param arr
     */
    public static void chooseSort(int[] arr){
        for(int i=0;i< arr.length;i++){
            int min=i;
            for(int j=i+1;j< arr.length;j++){
                if(arr[min]>arr[j]){
                    min = j;
                }
            }
            swap(arr,i,min);
        }
    }

    /**
     * 插入排序
     * @param arr
     * 4,2,1,6,3,8,10,0,5,99
     * 2,4,1
     * 2,1,4
     */
    public static void insertSort(int[] arr){
        for(int i=0;i< arr.length-1;i++){
           for(int j=i+1;j>0;j--){
               if(arr[j]<arr[j-1]){
                   swap(arr,j,j-1);
               }
           }
        }
    }



    public static void swap(int[] arr,int i,int j){
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j]=temp;
    }
}
