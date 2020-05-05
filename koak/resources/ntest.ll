declare void @putchar(i32)

define double @test(double) {

    ; unnamed block
    %tmpValue0 = fadd double 1.0, 2.0
    %tmpValue1 = fadd double %tmpValue0, %0
    ret double %tmpValue1

}

define i32 @main(i32, i8**) {

    ; unnamed block
    %tmpValue2 = call double @test(double 3.0)
    call void @putchar(i32 %tmpValue2)
    ret i32 0
}