package me.recursiveg.advelytra;

import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import scala.tools.cmd.gen.AnyValReps;

import java.util.Map;

public class CoreMod implements IFMLLoadingPlugin{
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{ASM.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    /* EntityLivingBase::moveEntityWithHeading#L1867(this.moveEntity()) */
    public static final class ASM implements IClassTransformer {
        @Override
        public byte[] transform(String name, String transformedName, byte[] basicClass) {
            byte[] ret;
            if ((ret = transform1(name, transformedName, basicClass)) != null) return ret;
            return basicClass;
        }

        private byte[] transform1(String name, String transformedName, byte[] basicClass) {
            try {
                if (!transformedName.equals("net.minecraft.entity.EntityLivingBase")) return null;
                ClassReader cr = new ClassReader(basicClass);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
                for (MethodNode mn : cn.methods) {
                    String methodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(name, mn.name, mn.desc);
                    String methodDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(mn.desc);

                    if ("func_70612_e(FF)V".equals(methodName + methodDesc) || "moveEntityWithHeading(FF)V".equals(methodName + methodDesc)) {
                        AbstractInsnNode n = mn.instructions.getFirst();
                        while (n != null) {
                            if (n instanceof MethodInsnNode) {
                                MethodInsnNode tmp = (MethodInsnNode) n;
                                if (tmp.getOpcode() == Opcodes.INVOKEVIRTUAL && tmp.desc.equals("(DDD)V")) {
                                    break;
                                }
                            }
                            n = n.getNext();
                        }
                        n = n.getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious();
                        mn.instructions.insertBefore(n, new VarInsnNode(Opcodes.ALOAD, 0));
                        mn.instructions.insertBefore(n, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/recursiveg/advelytra/AdvElytraCtl",
                                "beforeMotion","(Lnet/minecraft/entity/EntityLivingBase;)V", false));
                        System.out.println("[AEC] Transform success");
                    }
                }

                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                cn.accept(cw);
                return cw.toByteArray();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.print(String.format("Transform Error: (%s)%s", name, transformedName));
                return null;
            }
        }
    }
}
