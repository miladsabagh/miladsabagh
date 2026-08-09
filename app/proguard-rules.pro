-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Room generated implementations are looked up reflectively by name.
-keep class ir.zarrin.goldshop.data.local.**_Impl { *; }
