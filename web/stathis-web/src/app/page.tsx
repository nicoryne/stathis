'use client';

import { Navbar } from '@/components/navbar';
import { Footer } from '@/components/footer';
import { BackgroundTexture } from '@/components/background-texture';
import { FeatureCard } from '@/components/feature-card';
import { BenefitCard } from '@/components/benefit-card';
import { Button } from '@/components/ui/button';
import { motion } from 'framer-motion';
import {
  Activity,
  HeartPulse,
  Shield,
  Smartphone,
  Gauge,
  Award,
  Users,
  School
} from 'lucide-react';
import Image from 'next/image';
import Link from 'next/link';

export default function Home() {
  return (
    <div className="min-h-screen">
      <BackgroundTexture />
      <Navbar />

      {/* Hero Section */}
      <section className="pt-32 pb-16 md:pt-40 md:pb-20">
        <div className="container mx-auto px-4">
          <div className="flex flex-col items-center gap-12 md:flex-row">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5 }}
              className="flex-1 space-y-6"
            >
              <div className="bg-primary/10 text-primary inline-block rounded-full px-3 py-1 text-sm font-medium">
                AI-Powered Safety
              </div>
              <h1 className="text-4xl leading-tight font-bold md:text-5xl lg:text-6xl">
                Revolutionizing <span className="text-primary">Physical Education</span> with AI
              </h1>
              <p className="text-muted-foreground max-w-xl text-lg">
                Stathis combines motion recognition, real-time health tracking, and gamification to
                create a safer and more engaging physical education experience.
              </p>
              <div className="flex flex-col gap-4 pt-4 sm:flex-row">
                <Button size="lg" className="font-medium">
                  <Link href="/sign-in">Get Started</Link>
                </Button>
                <Button size="lg" variant="outline" className="font-medium">
                  Learn More
                </Button>
              </div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className="relative flex-1"
            >
              <div className="relative mx-auto aspect-square w-full max-w-md">
                <div className="from-primary/20 to-secondary/20 absolute inset-0 rounded-full bg-gradient-to-br blur-3xl" />
                <div className="bg-card/50 border-border relative overflow-hidden rounded-2xl border shadow-xl backdrop-blur-sm">
                  <Image
                    src="/placeholder.svg?height=600&width=600"
                    alt="Stathis App Interface"
                    width={600}
                    height={600}
                    className="h-auto w-full"
                  />
                  <div className="from-background/80 absolute right-0 bottom-0 left-0 bg-gradient-to-t to-transparent p-6">
                    <div className="flex items-center gap-3">
                      <div className="rounded-full bg-green-500/20 px-3 py-1 text-xs font-medium text-green-500">
                        Posture: Excellent
                      </div>
                      <div className="rounded-full bg-blue-500/20 px-3 py-1 text-xs font-medium text-blue-500">
                        Heart Rate: Normal
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="mx-auto mb-16 max-w-3xl text-center"
          >
            <div className="bg-secondary/10 text-secondary mb-4 inline-block rounded-full px-3 py-1 text-sm font-medium">
              Key Features
            </div>
            <h2 className="mb-4 text-3xl font-bold md:text-4xl">
              Transforming Physical Education with Technology
            </h2>
            <p className="text-muted-foreground">
              Stathis combines cutting-edge AI with health monitoring to create a safer, more
              engaging, and data-driven physical education experience.
            </p>
          </motion.div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            <FeatureCard
              icon={Activity}
              title="AI Posture Detection"
              description="Real-time analysis of student movements to ensure proper form and technique during exercises."
              delay={0.1}
            />
            <FeatureCard
              icon={HeartPulse}
              title="Vitals Monitoring"
              description="Track heart rate, blood pressure, and other health metrics through smartwatch integration."
              delay={0.2}
            />
            <FeatureCard
              icon={Shield}
              title="Safety Alerts"
              description="Immediate notifications when a student's health metrics reach concerning levels."
              delay={0.3}
            />
            <FeatureCard
              icon={Smartphone}
              title="Mobile Accessibility"
              description="Access exercise activities and health data through an intuitive mobile application."
              delay={0.4}
            />
            <FeatureCard
              icon={Gauge}
              title="Performance Analytics"
              description="Comprehensive data visualization to track progress and identify areas for improvement."
              delay={0.5}
            />
            <FeatureCard
              icon={Award}
              title="Gamified Experience"
              description="Interactive challenges and rewards to increase student engagement and motivation."
              delay={0.6}
            />
          </div>
        </div>
      </section>

      {/* Benefits Section */}
      <section id="benefits" className="bg-muted/30 py-20">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="mx-auto mb-16 max-w-3xl text-center"
          >
            <div className="bg-primary/10 text-primary mb-4 inline-block rounded-full px-3 py-1 text-sm font-medium">
              Benefits
            </div>
            <h2 className="mb-4 text-3xl font-bold md:text-4xl">
              Enhancing the PE Experience for Everyone
            </h2>
            <p className="text-muted-foreground">
              Stathis provides unique advantages for students, educators, and institutions alike.
            </p>
          </motion.div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            <BenefitCard
              title="Reduced Injury Risk"
              description="Real-time monitoring of movements and vital signs allows early detection of potential health issues."
              index={0}
              forWhom="For Students"
            />
            <BenefitCard
              title="Personalized Guidance"
              description="Receive exercise guidance based on individual fitness levels and health conditions."
              index={1}
              forWhom="For Students"
            />
            <BenefitCard
              title="Enhanced Supervision"
              description="Track multiple students simultaneously and intervene immediately when needed."
              index={2}
              forWhom="For Educators"
            />
            <BenefitCard
              title="Automated Assessment"
              description="Objective, data-driven evaluations of student performance save time and improve accuracy."
              index={3}
              forWhom="For Educators"
            />
            <BenefitCard
              title="Improved Safety Standards"
              description="Data-driven insights help improve PE policies and ensure student well-being."
              index={4}
              forWhom="For Schools"
            />
            <BenefitCard
              title="Resource Optimization"
              description="Analytics help optimize fitness programs and resource allocation for better outcomes."
              index={5}
              forWhom="For Schools"
            />
          </div>
        </div>
      </section>

      {/* About Section */}
      <section id="about" className="py-20">
        <div className="container mx-auto px-4">
          <div className="flex flex-col items-center gap-12 lg:flex-row">
            <motion.div
              initial={{ opacity: 0, x: -20 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5 }}
              viewport={{ once: true }}
              className="flex-1 space-y-6"
            >
              <div className="bg-secondary/10 text-secondary mb-4 inline-block rounded-full px-3 py-1 text-sm font-medium">
                Our Mission
              </div>
              <h2 className="mb-4 text-3xl font-bold md:text-4xl">
                Making Physical Education Safer and More Engaging
              </h2>
              <p className="text-muted-foreground">
                Stathis was developed to address the concerning prevalence of exercise-related
                injuries and fatalities in school settings. By leveraging AI and innovative
                technologies, we aim to transform physical education into a safer, more interactive,
                and data-driven experience.
              </p>
              <p className="text-muted-foreground">
                Our team of experts in education, health monitoring, and artificial intelligence has
                created a comprehensive solution that benefits students, educators, and institutions
                alike.
              </p>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: 20 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              viewport={{ once: true }}
              className="flex-1"
            >
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-primary/5 rounded-lg p-6 text-center">
                  <Users className="text-primary mx-auto mb-2 h-8 w-8" />
                  <h3 className="mb-1 font-medium">For Students</h3>
                  <p className="text-muted-foreground text-sm">
                    Safer, personalized exercise experience
                  </p>
                </div>
                <div className="bg-secondary/5 rounded-lg p-6 text-center">
                  <School className="text-secondary mx-auto mb-2 h-8 w-8" />
                  <h3 className="mb-1 font-medium">For Educators</h3>
                  <p className="text-muted-foreground text-sm">
                    Enhanced monitoring and assessment
                  </p>
                </div>
                <div className="bg-secondary/5 rounded-lg p-6 text-center">
                  <Shield className="text-secondary mx-auto mb-2 h-8 w-8" />
                  <h3 className="mb-1 font-medium">For Schools</h3>
                  <p className="text-muted-foreground text-sm">Improved safety standards</p>
                </div>
                <div className="bg-primary/5 rounded-lg p-6 text-center">
                  <Activity className="text-primary mx-auto mb-2 h-8 w-8" />
                  <h3 className="mb-1 font-medium">For Health</h3>
                  <p className="text-muted-foreground text-sm">Real-time vitals monitoring</p>
                </div>
              </div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="from-primary/10 to-secondary/10 bg-gradient-to-br py-20">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="mx-auto max-w-3xl text-center"
          >
            <h2 className="mb-4 text-3xl font-bold md:text-4xl">
              Ready to Transform Physical Education?
            </h2>
            <p className="text-muted-foreground mb-8">
              Join the revolution in PE monitoring and make exercise safer and more engaging for
              everyone.
            </p>

            <Button size="lg" className="font-medium">
              <Link href="/sign-in">Get Started</Link>
            </Button>
          </motion.div>
        </div>
      </section>

      <Footer />
    </div>
  );
}
