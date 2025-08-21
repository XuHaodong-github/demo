public static void main(String[] args) {
  // 正确的排序方式
  voList.sort(Comparator.comparing(CustomerServicePerformanceDetailVO::getMchtServerScore, Comparator.nullsLast(Comparator.reverseOrder())));        
}
