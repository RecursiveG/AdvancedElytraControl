package me.recursiveg.advelytra;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12")
public class CoreMod implements IFMLLoadingPlugin {
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

    /**
     * @see net.minecraft.entity.EntityLivingBase#travel(float, float, float)#L2076
     */
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

                    if ("func_191986_a(FFF)V".equals(methodName + methodDesc) || "travel(FFF)V".equals(methodName + methodDesc)) {
                        AbstractInsnNode n = mn.instructions.getFirst();
                        while (n != null) {
                            if (n instanceof MethodInsnNode) {
                                MethodInsnNode tmp = (MethodInsnNode) n;
                                if (tmp.getOpcode() == Opcodes.INVOKEVIRTUAL && tmp.desc.endsWith(";DDD)V")) {
                                    /* this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ); */
                                    break;
                                }
                            }
                            n = n.getNext();
                        }
                        n = n.getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious();
                        mn.instructions.insertBefore(n, new VarInsnNode(Opcodes.ALOAD, 0));
                        mn.instructions.insertBefore(n, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/recursiveg/advelytra/AdvElytraCtl",
                                "beforeMotion", "(Lnet/minecraft/entity/EntityLivingBase;)V", false));

                        while (n != null) {
                            if (n instanceof LdcInsnNode) {
                                LdcInsnNode tmp = (LdcInsnNode) n;
                                if (tmp.cst instanceof Float && Math.abs((float) tmp.cst - 0.91f) < 1E-5f)
                                    break;
                            }
                            n = n.getNext();
                        }
                        mn.instructions.insertBefore(n, new VarInsnNode(Opcodes.ALOAD, 0));
                        mn.instructions.insertBefore(n, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/recursiveg/advelytra/AdvElytraCtl",
                                "beforeNotFlyMotion", "(Lnet/minecraft/entity/EntityLivingBase;)V", false));

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
