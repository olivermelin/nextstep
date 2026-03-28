import { motion } from "framer-motion";
import { ReactNode } from "react";

interface PageTransitionProps {
  children: ReactNode;
}

const PageTransition = ({ children }: PageTransitionProps) => (
  <motion.div
    initial={{ opacity: 0, y: 8 }}
    animate={{ opacity: 1, y: 0 }}
    exit={{ opacity: 0, y: -8 }}
    transition={{ duration: 0.2, ease: [0.23, 1, 0.32, 1] }}
    className="h-full overflow-y-auto overflow-x-hidden pb-24 outline-none"
  >
    {children}
  </motion.div>
);

export default PageTransition;
